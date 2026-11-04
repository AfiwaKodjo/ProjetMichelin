# Projet Michelin

Afiwa Aimée KODJO

API Spring Boot pour gérer les restaurants du guide Michelin et leurs évaluations.

## Lancer

```
./mvnw spring-boot:run
```

Swagger : http://localhost:8080/swagger-ui/index.html

Tests : `./mvnw test`

## Utilisateurs

Keycloak, client `coursm2`

- lucien.bramard@michelin.fr / lasecuriteavantout (USER)
- noel.flantier@michelin.fr / cestbeaulhiver (ADMIN)

## Routes

| Méthode | Route | Qui |
|---|---|---|
| GET | /restaurants | tout le monde |
| GET | /restaurants/{id} | tout le monde |
| POST | /restaurants | ADMIN |
| PUT | /restaurants/{id} | ADMIN |
| POST | /restaurants/{id}/image | ADMIN |
| POST | /restaurants/{id}/evaluations | USER, ADMIN |
| PUT | /restaurants/{id}/evaluations/{evaluationId} | auteur ou ADMIN |
| DELETE | /restaurants/{id}/evaluations/{evaluationId} | auteur ou ADMIN |
| POST | /restaurants/{id}/evaluations/{evaluationId}/photos | auteur ou ADMIN |
| GET | /evaluations/moi | connecté |
| GET | /evaluations?motsCles=... | tout le monde |

## Explications

### Routes

Les GET sur les restaurants et la recherche sont ouverts à tout le monde, même sans être connecté. Pour moi c'est comme un guide, on doit pouvoir le consulter librement. Par contre pour écrire quoi que ce soit il faut un token.

J'ai mis les évaluations sous leur restaurant (`/restaurants/{id}/evaluations`) comme dans l'exemple des bibliothèques du cours. Seules la recherche et "mes évaluations" sont sous `/evaluations`, parce qu'elles ne dépendent pas d'un restaurant.

Pour les codes de retour j'ai mis 201 à la création et 204 à la suppression, comme ça on sait directement ce qui s'est passé.

### Ce que je n'ai pas fait / mes choix sur le sujet

- Je n'ai pas fait la suppression d'un restaurant, ce n'était pas dans le cahier des charges. Du coup `DELETE /restaurants/{id}` renvoie 405.
- La recherche se fait sur toutes les évaluations, pas par restaurant (vous aviez dit que les deux étaient possibles).
- Le nom de l'évaluateur n'est pas envoyé par le client, je le récupère dans le token avec le claim `name`. Sinon quelqu'un pourrait écrire au nom d'un autre.
- Les ids sont des entiers comme demandé dans le sujet.

### Sécurité

Spring ne lisait pas les rôles de Keycloak tout seul. J'ai cherché dans la doc de Spring Security et j'ai fait un convertisseur qui va chercher les rôles dans `resource_access.coursm2.roles` et qui ajoute `ROLE_` devant, pour que `hasRole('ADMIN')` marche.

Pour savoir si un USER peut modifier ou supprimer une évaluation, je compare l'auteur de l'évaluation avec le nom dans le token, ça se fait dans le service.

### S3

J'ai un seul `S3Repository` qui génère les liens (envoi et lecture), et chaque service lui donne son bucket : `restaurant` ou `evaluation`. L'API ne reçoit jamais les images, elle donne juste un lien valable 10 minutes et c'est le client qui envoie le fichier.

Comme les buckets sont partagés avec toute la promo, tous mes fichiers commencent par `Afiwa/` pour ne pas mélanger avec les autres.

### Erreurs

Tout passe par `GlobalExceptionHandler` et renvoie toujours `{ code, message }` : 400, 401, 403, 404, 405 et 500. Pour le 500 je log l'erreur mais je ne renvoie pas le détail au client.

J'ai eu un souci avec le 401 : sans token je recevais 403. En fait mon handler attrapait l'erreur avant Spring Security, donc je regarde maintenant si l'utilisateur est anonyme pour renvoyer 401.

### Recherche (Lucene)

Dans l'index je mets seulement l'id et le commentaire. Lucene me donne les ids et je récupère les évaluations dans la base, comme ça les données ne sont pas à deux endroits. L'index est mis à jour quand on ajoute, modifie ou supprime une évaluation, sinon la recherche trouvait encore les anciens commentaires.

### Tests

tests unitaires avec JUnit et Mockito, sur `RestaurantService`, `EvaluationService` et `IndexService`.

Pour les deux premiers j'ai mocké la base, S3 et Lucene. Pour `IndexService` je n'ai pas mocké parce que c'est justement Lucene que je voulais tester : chaque test crée son propre index dans un dossier à part (dans `target`), comme ça ça ne touche pas au vrai index.

J'ai supprimé le test `contextLoads` généré par Spring Initializr, il démarrait toute l'application et bloquait sur le verrou de l'index Lucene.

### Autres

- Java 21, Spring Boot 4.
- Les clés S3 sont dans `application.yaml` parce qu'elles étaient données dans le sujet, mais normalement il faudrait les mettre en variables d'environnement.
- Pour la moyenne, quand on récupère tous les restaurants, Hibernate charge les évaluations de chaque restaurant une par une (le problème N+1 vu en cours). Je ne l'ai pas corrigé, on pourrait faire une requête avec `AVG` directement en base.
