package gh.atsticks.samples.k8s.person;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * Wraps all the information needed to paginate a table.
 *
 * @author Roberto Cortez
 */
public class PaginatedListWrapper implements Serializable {
    private int currentPage;
    private int pageSize = 10;
    private long totalResults;

    private String sortFields;
    private String sortDirections;

    private List<Person> list;

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(long totalResults) {
        this.totalResults = totalResults;
    }

    public String getSortFields() {
        return sortFields==null?"id":sortFields;
    }

    public void setSortFields(String sortFields) {
        this.sortFields = sortFields;
    }

    public String getSortDirections() {
        return sortDirections==null?"asc":sortDirections;
    }

    public void setSortDirections(String sortDirections) {
        this.sortDirections = sortDirections;
    }

    public List getList() {
        return list;
    }

    public void setList(List<Person> list) {
        this.list = list;
    }
}
