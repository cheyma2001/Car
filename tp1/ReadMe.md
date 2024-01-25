# Serveur FTP

Il s'agit d'une implémentation simple d'un serveur FTP (File Transfer Protocol) en Java. Le serveur permet aux utilisateurs de se connecter, de s'authentifier et de récupérer des fichiers en utilisant le protocole FTP.

## Démarrage

### Prérequis

- Java Development Kit (JDK) installé sur votre machine
- Compréhension de base des commandes FTP

### Exécution du Serveur

Clonez ce dépôt :

```bash
git clone <https://github.com/cheyma2001/Car/>
```
Compilez le code du serveur :
```bash
javac FTPServer.java
```
Executez avec :
```bash
java FTPServer.java
```
Le serveur démarrera et écoutera sur le port 2020.

### Connexion au Serveur
Utilisez un client FTP pour vous connecter au serveur. Vous pouvez utiliser des clients FTP populaires tels que FileZilla, WinSCP, ou un client FTP en ligne de commande.
```bash
ftp localhost 2020
```
Suivez les invites du client FTP pour entrer le nom d'utilisateur et le mot de passe lorsqu'on vous le demande. Utilisez les noms d'utilisateur et les mots de passe prédéfinis suivants :

- Nom d'utilisateur : user1
- Mot de passe : pass1

### Commandes FTP

- **USER <nom-d'utilisateur> :** Entrez le nom d'utilisateur spécifié.
- **PASS <mot-de-passe> :** Entrez le mot de passe spécifié.
- **PASV :** Activez le mode passif et obtenez les détails de la connexion de données du serveur (Remarque : La commande PASV est recommandée par rapport à EPSV pour une compatibilité plus large et est le mode par défaut).
- **RETR <nom-de-fichier> :** Récupérez le fichier spécifié à partir du serveur (Remarque : La commande RETR ne fonctionne actuellement pas correctement).
- **QUIT :** Déconnectez-vous du serveur FTP.

### Problèmes avec le Mode Passif

Si vous rencontrez des problèmes avec les connexions en mode passif, en particulier si votre client FTP ne prend pas en charge EPSV, considérez ce qui suit :

- Assurez-vous que votre client FTP est configuré pour utiliser le mode passif (PASV) plutôt que EPSV.
- Vérifiez les paramètres de votre pare-feu pour autoriser les connexions entrantes à la plage de ports dynamique utilisée pour le transfert de données en mode passif.


## Exemple d'utilisation
* Ouvrez un nouveau bash ou prompt
```bash
ftp localhost 2020

user user1
pass pass1
pasv
retr Car/tp1/FTPServer.java
quit
```
