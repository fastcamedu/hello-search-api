package com.fastcampus.hellosearchapi.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.SourceConfig;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import com.fastcampus.hellosearchapi.domain.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProductApiController {
    private final ElasticsearchClient esClient;

    @Value("${fastcampus.search.index}")
    private String searchIndex;

    @GetMapping(value = "/search")
    public List<Product> findAll(@RequestParam(name = "q") String keyword) throws IOException {
        log.info(">>> Search Keyword : {}", keyword);
        SourceConfig sourceConfig = SourceConfig.of(sc -> sc.filter(sf -> sf.includes("id", "name")));
        SearchResponse<Product> response = esClient.search(s -> s
            .index(searchIndex)
            .source(sourceConfig)
            .query(q -> q
                    .match(t -> t
                            .field("product_full_desc")
                            .query(keyword)
                    )
            )
            ,
            Product.class
        );
        TotalHits totalHits = response.hits().total();
        boolean isExactResult = totalHits.relation() == TotalHitsRelation.Eq;
        if (isExactResult) {
            log.info(">>> Exact Result: {}", totalHits.value());
        } else {
            log.info(">>> More than result: {}", totalHits.value());
        }

        List<Hit<Product>> hits = response.hits().hits();
        ArrayList<Product> products = new ArrayList<>();
        for (Hit<Product> hit: hits) {
            Product product = hit.source();
            log.info(">>> ????????? ??????, {}", product);
            products.add(product);
        }
        return products;
    }

    @GetMapping(value = "/exists/{indexName}/{documentId}")
    public boolean exists(@PathVariable(name = "indexName") String indexName, @PathVariable(name = "documentId") String documentId) throws IOException {
        log.info(">>> ?????? ID ?????? ?????? : {}, {}", indexName, documentId);
        return esClient.exists(b -> b.index(indexName)
                        .id(documentId))
                .value();
    }

    @GetMapping(value = "/documents/{indexName}/{documentId}")
    public ResponseEntity<Product> findDocumentById(@PathVariable(name = "indexName") String indexName, @PathVariable(name = "documentId") String documentId) throws IOException {
        log.info(">>> ?????? ID ?????? : {}, {}", indexName, documentId);
        GetResponse<Product> response = esClient.get(g -> g
                    .index(indexName)
                    .source(s -> s.fields(Arrays.asList("id", "name")))
                    .id(documentId),
            Product.class
        );

        if (response.found()) {
            Product product = response.source();
            log.info(">>> ????????? :  " + product.getName());
            return ResponseEntity.of(Optional.of(product));
        } else {
            log.info(">>> ????????? ?????? ?????????");
            return ResponseEntity.of(Optional.of(null));
        }
    }
}
