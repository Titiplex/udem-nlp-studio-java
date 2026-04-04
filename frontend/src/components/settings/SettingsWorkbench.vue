<template>
  <section class="settings-shell">
    <h2>Settings</h2>
    <div class="card">
      <p>
        Ce panneau reste volontairement simple dans le lot 1.
        Il servira au lot suivant pour les chemins de règles, imports/exports,
        profils de projet et préférences d’interface.
      </p>
    </div>
  </section>
</template>

<style scoped>
.settings-shell {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card {
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 16px;
  padding: 18px;
}
</style>