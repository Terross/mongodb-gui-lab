package ru.leti.mongodb.gui.controller;

import com.mongodb.client.MongoClient;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Indexes.ascending;
import static com.mongodb.client.model.Indexes.descending;
import static com.mongodb.client.model.Projections.include;
import static org.apache.commons.lang3.math.NumberUtils.isParsable;
import static org.apache.commons.lang3.math.NumberUtils.toDouble;

@Controller
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DataBaseController {

    private final MongoClient mongoClient;

    @GetMapping("/api/database")
    public ResponseEntity<List<String>> getDatabaseList() {
        return ResponseEntity.ok(newArrayList(mongoClient.listDatabaseNames()));
    }

    @GetMapping("/api/collection/{database}")
    public ResponseEntity<List<String>> getCollectionsInDatabase(@PathVariable String database) {
        var mongoDatabase = mongoClient.getDatabase(database);
        return ResponseEntity.ok(newArrayList(mongoDatabase.listCollectionNames()));
    }

    @GetMapping("/api/document/{database}/{collection}")
    public ResponseEntity<List<Document>> getDocumentsInCollection(@PathVariable String database,
                                                                   @PathVariable String collection,
                                                                   @RequestParam Map<String, String> keyValueMap,
                                                                   @RequestParam(defaultValue = "_id") String sortField,
                                                                   @RequestParam(defaultValue = "asc") String sortDirection,
                                                                   @RequestParam(defaultValue = "eq") String condition,
                                                                   @RequestParam(defaultValue = "and") String operator) {
        keyValueMap.remove("condition");
        keyValueMap.remove("operator");
        keyValueMap.remove("sortField");
        keyValueMap.remove("sortDirection");
        var mongoDatabase = mongoClient.getDatabase(database);
        var mongoCollection = mongoDatabase.getCollection(collection);
        if (keyValueMap.size() == 0) {
            return ResponseEntity.ok(newArrayList(mongoCollection.find()
                    .sort(sortDirection.equals("asc") ? ascending(sortField) : descending(sortField))));
        }
        var filter = getMongoFilter(keyValueMap, condition, operator);
        return ResponseEntity.ok(newArrayList(mongoCollection
                .find()
                .filter(filter)
                .sort(sortDirection.equals("asc") ? ascending(sortField) : descending(sortField))));
    }

    @GetMapping("/api/document/projection/{database}/{collection}")
    public ResponseEntity<?> getDocumentProjectionInCollection(@PathVariable String database,
                                                               @PathVariable String collection,
                                                               @RequestParam List<String> keys) {
        var mongoDatabase = mongoClient.getDatabase(database);
        var mongoCollection = mongoDatabase.getCollection(collection);
        return ResponseEntity.ok(newArrayList(mongoCollection.find().projection(include(keys))));
    }

    private Bson getMongoFilter(Map<String, String> keyValueMap, String condition, String operator) {
        return switch (operator) {
            case "or" -> or(getFilters(keyValueMap, condition));
            case "not" -> not(getFilter(keyValueMap.entrySet().stream().findFirst().get(), condition));
            default -> and(getFilters(keyValueMap, condition));
        };
    }

    private List<Bson> getFilters(Map<String, String> keyValueMap, String condition) {
        return keyValueMap
                .entrySet()
                .stream()
                .map(keyValue -> getFilter(keyValue, condition))
                .toList();
    }

    private Bson getFilter(Map.Entry<String, String> keyValue, String condition) {
        return switch (condition) {
            case "gt" -> gt(keyValue.getKey(), getValue(keyValue));
            case "lt" -> lt(keyValue.getKey(), getValue(keyValue));
            default -> eq(keyValue.getKey(), getValue(keyValue));
        };
    }

    private Serializable getValue(Map.Entry<String, String> keyValue) {
        return isParsable(keyValue.getValue())
                ? toDouble(keyValue.getValue())
                : keyValue.getValue();
    }
}
