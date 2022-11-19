<template>
  <v-app>
    <v-app-bar app>
      <v-app-bar-title>Mongo</v-app-bar-title>
    </v-app-bar>

    <!-- Sizes your content based upon application components -->
    <v-main>
      <mongo-component></mongo-component>
    </v-main>
  </v-app>
</template>

<script>
import MongoComponent from "@/components/MongoComponent";

export default {
  name: "MainComponent",
  components: {
    MongoComponent
  }
}
</script>

<style scoped>

</style>