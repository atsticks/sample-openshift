package gh.atsticks.samples.k8s.common;

import io.fabric8.kubernetes.api.model.EndpointsList;
import io.fabric8.kubernetes.api.model.Service;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;


/**
 * Created by amo on 06.04.17.
 */
public final class DiscoveryClient {

  private static final String DEFAULT_NAMESPACE = "default";
  private final String namespace;
  private final Kubernetes client;
  private final Logger logger = Logger.getLogger(DiscoveryClient.class.getName());

  DiscoveryClient(Kubernetes kubernetes) {
    namespace = DEFAULT_NAMESPACE;
    client = Objects.requireNonNull(kubernetes);
  }

  DiscoveryClient(Kubernetes kubernetes,
                          String namespace) {
    this.namespace = namespace != null ? namespace : DEFAULT_NAMESPACE;
    this.client = Objects.requireNonNull(kubernetes);
  }

  public String getNamespace(){
    return namespace;
  }

  public void findServiceByName(String serviceName, Consumer<Service> serviceConsumer,
      Consumer<Throwable> error) {
    Objects.requireNonNull(client, "no client available");
    final Optional<Service> serviceEntryOptional = client
        .getServices()
        .getItems()
        .stream()
        .filter(item -> item.getMetadata().getNamespace().equalsIgnoreCase(namespace))
        .filter(item -> item.getMetadata().getName().equalsIgnoreCase(serviceName))
        .findFirst();

    if (!serviceEntryOptional.isPresent()) {
      error.accept(new Throwable("no service with name " + serviceName + " found"));
    }
    serviceEntryOptional.ifPresent(serviceConsumer::accept);

  }

  public void findServiceByLabel(String label, Consumer<Service> serviceConsumer,
      Consumer<Throwable> error) {
    Objects.requireNonNull(client, "no client available");
    final Optional<Service> serviceEntryOptional = client
        .getServices()
        .getItems()
        .stream()
        .filter(item -> item.getMetadata().getNamespace().equalsIgnoreCase(namespace))
        .filter(item -> item.getMetadata().getLabels().keySet().stream()
            .filter(key -> key.equalsIgnoreCase(label)).findFirst().isPresent())
        .findFirst();

    if (!serviceEntryOptional.isPresent()) {
      error.accept(new Throwable("no service with label " + label + " found"));
    }
    serviceEntryOptional.ifPresent(serviceConsumer::accept);
  }

  public void findEndpointsByLabel(String label, Consumer<EndpointsList> endpointsListConsumer,
      Consumer<Throwable> error) {
    Objects.requireNonNull(client, "no client available");
    final EndpointsList serviceEntryOptional = client
        .getEndpoints(namespace, label);

    if (serviceEntryOptional == null) {
      error.accept(new Throwable("no service with label " + label + " found"));
    }
    endpointsListConsumer.accept(serviceEntryOptional);
  }


  public void resolveAnnotations(Object bean) {
    Objects.requireNonNull(client, "no client available");
    final List<Field> serverNameFields = client.findServiceFields(bean);
    final List<Field> labelFields = client.findLabelields(bean);
    if (!serverNameFields.isEmpty())
      client.findServiceEntryAndSetValue(bean, serverNameFields, namespace);

    if (!labelFields.isEmpty())
      client.findServiceLabelAndSetValue(bean, labelFields, namespace);

  }
}
