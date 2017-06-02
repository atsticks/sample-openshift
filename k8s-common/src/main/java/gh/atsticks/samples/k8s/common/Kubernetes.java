package gh.atsticks.samples.k8s.common;

import io.fabric8.annotations.ServiceName;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by atsticks.
 */
public final class Kubernetes {

  private static final Logger LOG = Logger.getLogger(Kubernetes.class.getName());

  public static final String SEPERATOR = ":";
  private static final String IO_SERVICEACCOUNT_TOKEN = "/var/run/secrets/kubernetes.io/serviceaccount/token";
  private static final String DEFAULT_MASTER_URL = "https://kubernetes.default.svc";

  private KubernetesClient kubernetesClient;


  private Kubernetes(KubernetesClient client){
    this.kubernetesClient = Objects.requireNonNull(client);
  }

  public static Kubernetes of() {
    return of(null, DEFAULT_MASTER_URL);
  }

  public static Kubernetes of(String apiToken) {
    return of(apiToken, DEFAULT_MASTER_URL);
  }

  public static Kubernetes of(String apiToken, String kubernetesMaster) {
    if(kubernetesMaster==null){
      kubernetesMaster = DEFAULT_MASTER_URL;
    }
    String oauthToken = apiToken;
    if (StringUtil.isNullOrEmpty(oauthToken)) {
      oauthToken = getAccountToken();
    }
    if (StringUtil.isNullOrEmpty(oauthToken)) {
      return null;
    }
    LOG.info("Kubernetes Discovery: Bearer Token { " + apiToken + " }");
    Config config = new ConfigBuilder().withOauthToken(oauthToken).withMasterUrl(kubernetesMaster)
        .build();
    return new Kubernetes(new DefaultKubernetesClient(config));
  }

  public DiscoveryClient getDiscoveryClient(String namespace){
    return new DiscoveryClient(this, namespace);
  }

  private static String getAccountToken() {
    try {
      final Path path = Paths.get(IO_SERVICEACCOUNT_TOKEN);
      if (!path.toFile().exists()) {
        return null;
      }
      return new String(Files.readAllBytes(path));

    } catch (IOException e) {
      throw new RuntimeException("Could not get token file", e);
    }
  }

  public KubernetesClient getKubernetesClient() {
    return kubernetesClient;
  }

  public PodList getPods(String namespace) {
    return kubernetesClient.pods().inNamespace(namespace).list();
  }

  public PodList getPods(String namespace, String label) {
    return kubernetesClient.pods().inNamespace(namespace).withLabel(label).list();
  }

  public PodList getPods(String namespace, String label, String value) {
    return kubernetesClient.pods().inNamespace(namespace).withLabel(label, value).list();
  }

  public EndpointsList getEndpoints(String namespace) {
    return kubernetesClient.endpoints().inNamespace(namespace).list();
  }

  public EndpointsList getEndpoints() {
    return kubernetesClient.endpoints().list();
  }

  public EndpointsList getEndpoints(String namespace, String label) {
    return kubernetesClient.endpoints().inNamespace(namespace).withLabel(label).list();
  }

  public ServiceList getServices(){
    return kubernetesClient.services().list();
  }

  public ServiceList getServices(String namespace){
    return kubernetesClient.services().inNamespace(namespace).list();
  }

  public ServiceList getServices(String namespace, String label){
    return kubernetesClient.services().inNamespace(namespace).withLabel(label).list();
  }

  public ServiceList getServices(String namespace, String label, String value){
    return kubernetesClient.services().inNamespace(namespace).withLabel(label, value).list();
  }

  public EndpointsList getEndpoints(String namespace, String label, String value) {
    return label != null && !value.isEmpty() ?
            kubernetesClient.endpoints().inNamespace(namespace).withLabel(label, value).list() :
            kubernetesClient.endpoints().inNamespace(namespace).withLabel(label).list();
  }

  public void resolveServiceAnnotationsAndInit(Object bean, String namespace) {
    final List<Field> serverNameFields = findServiceFields(bean);
    final List<Field> labelFields = findLabelields(bean);
    if (!serverNameFields.isEmpty()) {
      findServiceEntryAndSetValue(bean, serverNameFields, namespace);
    }
    if (!labelFields.isEmpty()) {
      findServiceLabelAndSetValue(bean, labelFields, namespace);
    }
  }

  public void findServiceEntryAndSetValue(Object bean, List<Field> serverNameFields,
                                          String namespace) {
    serverNameFields.forEach(serviceNameField -> {
      final ServiceName serviceNameAnnotation = serviceNameField
              .getAnnotation(ServiceName.class);
      final String serviceName = serviceNameAnnotation.value();
      final Optional<Service> serviceEntryOptional = findServiceEntry(serviceName,
              namespace);

      serviceEntryOptional.ifPresent(serviceEntry -> {
        String hostString = getServiceHost(serviceEntry);
        setFieldValue(bean, serviceNameField, hostString);
      });
    });
  }

  public void findServiceLabelAndSetValue(Object bean, List<Field> serverNameFields,
                                          String namespace) {
    serverNameFields.forEach(serviceNameField -> {
      final Label serviceNameAnnotation = serviceNameField
              .getAnnotation(Label.class);
      final String labelName = serviceNameAnnotation.name();
      final String labelValue = serviceNameAnnotation.value();

      setServiceEndpoints(bean, namespace, serviceNameField, labelName, labelValue);

      setServicePods(bean, namespace, serviceNameField, labelName, labelValue);

    });
  }

  private void setServicePods(Object bean, String namespace,
                              Field serviceNameField, String labelName, String labelValue) {
//    if (serviceNameField.getType().isAssignableFrom(Pods.class)) {
      PodList pods = getPods(namespace, labelName, labelValue);
      setFieldValue(bean, serviceNameField, pods);
//    }
  }

  private void setServiceEndpoints(Object bean, String namespace,
                                   Field serviceNameField, String labelName, String labelValue) {
//    if (serviceNameField.getType().isAssignableFrom(Endpoints.class)) {
      EndpointsList endpoint = getEndpoints(namespace,labelName,labelValue);
      setFieldValue(bean, serviceNameField, endpoint);
//    }
  }

  public static List<Field> findServiceFields(Object bean) {
    return Stream.of(bean.getClass().getDeclaredFields())
            .filter((Field filed) -> filed.isAnnotationPresent(ServiceName.class))
            .collect(Collectors.toList());
  }

  public static List<Field> findLabelields(Object bean) {
    return Stream.of(bean.getClass().getDeclaredFields())
            .filter((Field filed) -> filed.isAnnotationPresent(Label.class))
            .collect(Collectors.toList());
  }

  public static void setFieldValue(Object bean, Field serviceNameField, Object value) {
    serviceNameField.setAccessible(true);
    try {
      serviceNameField.set(bean, value);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  private Optional<Service> findServiceEntry(String serviceName,
                                             String namespace) {
    return kubernetesClient
            .services()
            .inNamespace(namespace)
            .list()
            .getItems()
            .stream()
            .filter(item -> item.getMetadata().getName().equalsIgnoreCase(serviceName))
            .findFirst();
  }


  private String getServiceHost(Service serviceEntry) {
    String hostString = "";
    final String clusterIP = serviceEntry.getSpec().getClusterIP();
    final List<ServicePort> ports = serviceEntry.getSpec().getPorts();
    if (!ports.isEmpty()) {
      hostString = clusterIP + SEPERATOR + ports.get(0).getPort();
    }
    return hostString;
  }
}