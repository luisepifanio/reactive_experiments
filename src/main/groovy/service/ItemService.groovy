package service

import dto.Item
import groovyx.net.http.RESTClient
import groovy.util.slurpersupport.GPathResult
// import static groovyx.net.http.ContentType.XML
import static groovyx.net.http.ContentType.JSON

class ItemService {
    Item getById(Long id){
        RESTClient solr = new RESTClient('http://localhost:8080/solr/update')
        def response = solr.post(
                contentType: JSON,
                requestContentType: JSON,
                body: {
                    add {
                        doc {
                            field(name:"id", "SOLR1000")
                            field(name:"name", "Solr, the Enterprise Search Server")
                            field(name:"manu", "Apache Software Foundation")
                            field(name:"cat", "software")
                            field(name:"cat", "search")
                            field(name:"features", "Advanced Full-Text Search Capabilities using Lucene")
                            field(name:"features", "Optimized for High Volume Web Traffic")
                            field(name:"features", "Standards Based Open Interfaces - XML and HTTP")
                            field(name:"features", "Comprehensive HTML Administration Interfaces")
                            field(name:"features", "Scalability - Efficient Replication to other Solr Search Servers")
                            field(name:"features", "Flexible and Adaptable with XML configuration and Schema")
                            field(name:"features", "Good unicode support: héllo (hello with an accent over the e)")
                            field(name:"price", "0")
                            field(name:"popularity", "10")
                            field(name:"inStock", "true")
                            field(name:"incubationdate_dt", "2006-01-17T00:00:00.000Z")
                        }
                    }
                }
        )

        assert response.success
        assert response.status == 200
        assert response.data instanceof GPathResult

        println "Solr response status: ${response.status}"
    }
}
