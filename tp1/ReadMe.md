# Serveur FTP Java

Ceci est une implémentation simple d'un serveur FTP en Java qui prend en charge diverses commandes FTP de base.

## Prérequis

- WSL si vous utiliser Windows.

## Comment exécuter

1. **Compiler le code :**

    ```bash
    javac tp1/FTPServeur.java
    ```

2. **Exécuter le serveur :**

    ```bash
    java tp1.FTPServeur
    ```

3. **Connexion au serveur :**

   Utilisez un client FTP pour vous connecter au serveur.
   * exemple : Se connecter avec un nouveau prompt :
   ```bash
   $ ftp localhost 2121
    Trying [::1]:2121 ...
    Connected to localhost.
    220 Service ready 
    Name (localhost:passwd): user1
    331 User name okay, need password
    Password: 
    230 User logged in
   ```


## Commandes FTP prises en charge

- **USER username**
    - Exemple : `USER user1`
    - Authentifie l'utilisateur avec le nom d'utilisateur fourni.

- **PASS password**
    - Exemple : `PASS pass1`
    - Fournit le mot de passe pour l'authentification.

- **EPSV**
    - Exemple : `EPSV` 
    - Entre en mode passif étendu.

- **RETR filename**
    - Exemple : `RETR example.txt`
    - Récupère le fichier spécifié depuis le serveur.

- **LIST**
    - Exemple : `LIST`
    - Liste les fichiers dans le répertoire actuel.

- **CWD directory**
    - Exemple : `CWD /chemin/vers/repertoire`
    - Change le répertoire de travail vers celui spécifié.

- **QUIT**
    - Exemple : `QUIT`
    - Se déconnecte et ferme la connexion.

## Informations d'identification de l'utilisateur

Les informations d'identification de l'utilisateur sont prédéfinies dans la map `userCredentials` dans le code. Modifiez cette map pour ajouter ou supprimer des utilisateurs.

```java
private static final Map<String, String> userCredentials = new HashMap<>();

static {
    // Ajoutez les utilisateurs et mots de passe ici
    userCredentials.put("user1", "pass1");
    userCredentials.put("user2", "pass2");
}
```
## Extension du Programme pour Ajouter des Commandes FTP

Pour étendre le programme afin de prendre en charge des commandes FTP non implémentées, suivez ces étapes :

1. **Ajouter une Nouvelle Commande :** Identifiez la nouvelle commande FTP que vous souhaitez prendre en charge. Créez ensuite une méthode dans votre code pour gérer cette commande. Par exemple, si vous souhaitez ajouter la commande MYCMD, créez une méthode `handleMyCmdCommand` qui traitera cette commande.

2. **Intégrer la Nouvelle Commande dans le Switch :** Ajoutez une nouvelle branche dans la structure switch qui vérifie la commande reçue et appelle la méthode appropriée pour la traiter. Par exemple :

    ```java
    switch (command) {
        // ... Autres commandes existantes
        case "MYCMD":
            handleMyCmdCommand(scanner, outputStream);
            break;
        default:
            sendResponse(outputStream, "502 Command not implemented\r\n");
    }
    ```

3. **Implémenter la Logique de la Nouvelle Commande :** À l'intérieur de la méthode que vous avez créée, implémentez la logique spécifique à la nouvelle commande. Cela pourrait inclure la validation des arguments, l'exécution de l'action associée à la commande, et l'envoi de la réponse appropriée au client.

4. **Tester et Valider :** Testez soigneusement la nouvelle commande pour vous assurer qu'elle fonctionne comme prévu. Vérifiez également la gestion des erreurs et assurez-vous que le serveur continue de fonctionner correctement avec d'autres commandes existantes.

6. **Documenter :** Ajoutez des commentaires détaillés dans le code pour expliquer la logique de la nouvelle commande, ses exigences et son fonctionnement.


## Remarque

* Le serveur s'exécute par défaut sur le port 2121. Assurez-vous que ce port est disponible et n'est pas bloqué par un pare-feu.

### Credit :
* CHEYMA ZEKHNINI