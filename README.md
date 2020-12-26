# doc-search
A doc searcher of the documents on the local host that is based on: Tika, ElasticSearch and Kibana
# ElasticSearch
[Install Elasticsearch with Dockeredit](https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html)  
Testing: `curl -X GET "localhost:9200/_cat/nodes?v&pretty"`  
[Java REST Client](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.10/index.html)
# Kibana
UI dashboard for Elastic Search
[Install Kibana with Dockeredit](https://www.elastic.co/guide/en/kibana/current/docker.html)

# Tika Server
This is a Spring Boot application the main tasks of that are:  
 - scanning every 1 minute the files in the special folder
 - extracting a content of the files using Apache Tika
 - storing the medata and the content of the files in the Elasticsearch 
