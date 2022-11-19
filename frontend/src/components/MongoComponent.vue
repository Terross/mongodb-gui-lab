<template>
  <v-container>
    <v-row no-gutters justify="space-between">
      <v-col>
        <v-select
            class="ma-2"
            v-model="database"
            label="Базы данных"
            :items="databases"
        ></v-select>
      </v-col>
      <v-col>
        <v-select
            class="ma-2"
            v-model="collection"
            label="Коллекции"
            :items="collections"
        ></v-select>
      </v-col>
    </v-row>
    <v-row justify="start">
      <v-col md="4">
        <v-btn
            :onclick="allDocuments"
            color="primary"
        >
          Вывести все документы
        </v-btn>
      </v-col>
    </v-row>
    <v-row justify="start">
      <v-col md="4">
        <v-btn
            :onclick="allDocumentsWithKeys"
            color="primary"
        >
          Вывести заданные ключи
        </v-btn>
      </v-col>
      <v-col md="4">
        <v-text-field
            v-model="keyList"
            label="Ключи"
        ></v-text-field>
      </v-col>
    </v-row>
    <v-row justify="start">
      <v-col md="4">
        <v-btn
            class="ma-2"
            size="large"
            :onclick="allDocumentsWithFilters"
            color="primary"
        >
          Вывести документы
        </v-btn>
        <v-select
            class="ma-2"
            v-model="condition"
            label="Условие"
            :items="conditions"
        ></v-select>
        <v-select
            class="ma-2"
            v-model="operator"
            label="Операция"
            :items="operators"
        ></v-select>
        <v-text-field
            class="ma-2"
            v-model="sortField"
            label="Ключ для сортировки"
        ></v-text-field>
        <v-select
            class="ma-2"
            v-model="sortDirection"
            label="Направление сортировки"
            :items="sortDirections"
        ></v-select>
      </v-col>
      <v-col md="4">
        <v-text-field
            v-model="key1"
            label="Ключ 1"
        ></v-text-field>
        <v-text-field
            v-model="value1"
            label="Значение 1"
        ></v-text-field>
      </v-col>
      <v-col md="4">
        <v-text-field
            v-model="key2"
            label="Ключ 2"
        ></v-text-field>
        <v-text-field
            v-model="value2"
            label="Значение 2"
        ></v-text-field>
      </v-col>
    </v-row>
    <vue-json-pretty v-if="bson.length > 0" :data="bson"></vue-json-pretty>

  </v-container>

</template>

<script>
import {HTTP} from "@/http/http-common";
import VueJsonPretty from 'vue-json-pretty';
import 'vue-json-pretty/lib/styles.css';

export default {
  name: 'MongoComponent',
  components: {
    VueJsonPretty,
  },
  props: {
    msg: String
  },
  data: () => ({
    databases: [],
    collections: [],
    database: "",
    collection: "",
    bson: [],
    keyList: [],
    key1: "",
    key2: "",
    value1: "",
    value2: "",
    conditions: ["lt", "gt", "eq"],
    condition: "eq",
    operators: ["or", "and"],
    operator: "and",
    sortField: "_id",
    sortDirections: ["asc", "desc"],
    sortDirection: "asc"
  }),
  methods: {
    allDocuments() {
      HTTP.get(`/document/${this.database}/${this.collection}`).then(response => {
        this.bson = response.data
      })
    },
    allDocumentsWithFilters() {
      const params = new URLSearchParams([
        [this.key1, this.value1],
        [this.key2, this.value2],
        ["condition", this.condition],
        ["operator", this.operator],
        ["sortField", this.sortField],
        ["sortDirection", this.sortDirection],
      ]);
      HTTP.get(`/document/${this.database}/${this.collection}`, {params}).then(response => {
        this.bson = response.data
      })
    },
    allDocumentsWithKeys() {
      HTTP.get(`/document/projection/${this.database}/${this.collection}`, {params: {keys: this.keyList}}).then(response => {
        this.bson = response.data
      })
    }
  },
  beforeCreate() {
    HTTP.get('/database').then(response => {
      this.databases = response.data
    })
  },
  watch: {
    database() {
      HTTP.get(`/collection/${this.database}`).then(response => {
        this.collections = response.data
      })
    }
  }

}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h3 {
  margin: 40px 0 0;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
</style>
