# Go4Lunch

**Application mobile Android pour faciliter la planification de déjeuners en équipe en sélectionnant des restaurants à proximité.**

---

## Vue d'ensemble

**Go4Lunch** est une application collaborative destinée aux employés qui souhaitent organiser leurs déjeuners en groupe en sélectionnant des restaurants proches et en partageant leurs choix avec leurs collègues. Elle permet de visualiser les préférences de chaque membre de l’équipe et de coordonner les plans de déjeuner via des notifications de rappel.

---

## Fonctionnalités

1. **Authentification sécurisée** : Connexion via un compte Google pour vérifier l’identité de chaque utilisateur.
2. **Recherche et sélection de restaurants** :
   - Affichage des restaurants à proximité avec une carte interactive.
   - Option de marquer un restaurant comme favori.
3. **Coordination des collègues** :
   - Affichage des choix de restaurants de chaque utilisateur.
   - Possibilité de rejoindre le même restaurant que d’autres collègues.
4. **Notifications de rappel** : Envoi de notifications avant le déjeuner pour rappeler aux utilisateurs l’adresse et les collègues associés au restaurant sélectionné.
5. **Interface multilingue** : Disponible en français et en anglais.

---

## Architecture et intégration back-end

L’application utilise **Firebase** pour les services de back-end suivants :

- **Authentification** : Connexion via OAuth Google.
- **Base de données en temps réel** : Stockage des informations de restaurant et des préférences utilisateur.
- **Notifications push** : Rappel de la réservation de restaurant pour le déjeuner.

> **Important** : L’accès aux fonctionnalités de Firebase est restreint aux utilisateurs connectés, conformément aux Firebase rules.

---

## Vue principale et interface utilisateur

L’application comporte trois vues principales, accessibles par des boutons en bas de l’écran :

1. **Vue Carte** : Affiche les restaurants à proximité avec géolocalisation automatique et des marqueurs personnalisés pour les restaurants choisis.
2. **Vue Liste** : Liste des restaurants avec détails comme la distance, l’adresse, les avis, et le nombre de collègues intéressés.
3. **Vue des collègues** : Montre les choix de restaurant de chaque utilisateur connecté.

Chaque vue possède une fonction de recherche intégrée pour trouver un restaurant spécifique.

---

## Fonctionnalités complémentaires

En plus des fonctionnalités de base, les options suivantes peuvent être implémentées :

- **Tri avancé des restaurants** : Selon des critères comme la distance, la popularité, ou le type de cuisine.
- **Chat intégré** : Permettre aux utilisateurs de discuter et de finaliser leurs plans de déjeuner.
- **Authentification Twitter** : Une option d’authentification complémentaire.

---

## Contraintes et optimisation

**Green code et économies d’énergie** :

- L’appel API pour récupérer la liste des restaurants est effectué une seule fois au démarrage, et les données sont mises en cache pour éviter les appels répétés.
- L’application est optimisée pour une faible consommation de batterie et de données, en tenant compte des appareils plus anciens.

**Compatibilité** :

- Fonctionne sur toutes les tailles d’écran Android en mode portrait.
- Prise en charge d’Android 5.0 (Lollipop) et versions ultérieures.

---

## Installation et configuration

### Prérequis

- **Android Studio** : Version Hedgehog (2023.1.1) ou plus récente.
- **SDK Android minimum** : API 21 (Android 5.0).
- **Firebase** : Configurer la base de données Firebase et ajouter le fichier `google-services.json` au projet.

### Étapes d’installation

1. **Cloner le dépôt** :
   ```bash
   git clone https://github.com/CedricHaegele/OC_P7_Go4Lunch.git


<img width="209" alt="image" src="https://github.com/CedricHaegele/OC_P7_Go4Lunch/assets/85683236/96dec6c5-325c-49ec-a152-4e82eaebb5a8">
<img width="208" alt="image" src="https://github.com/CedricHaegele/OC_P7_Go4Lunch/assets/85683236/d95c07c2-3c60-4497-b7c7-268ebcf927af">
<img width="207" alt="image" src="https://github.com/CedricHaegele/OC_P7_Go4Lunch/assets/85683236/87db2227-8c29-4f05-8f09-ead517d37f7f">
<img width="217" alt="image" src="https://github.com/CedricHaegele/OC_P7_Go4Lunch/assets/85683236/ff4d18ac-1343-41a5-9963-205b82ce5633">
<img width="215" alt="image" src="https://github.com/CedricHaegele/OC_P7_Go4Lunch/assets/85683236/5b2bae47-e43b-40b0-84c3-b77ae4272249">
<img width="212" alt="image" src="https://github.com/CedricHaegele/OC_P7_Go4Lunch/assets/85683236/0dd90e03-899d-474d-b0b9-1b4bbebeed37">






