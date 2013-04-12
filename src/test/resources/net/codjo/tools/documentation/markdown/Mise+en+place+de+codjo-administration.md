## Installation

Cette page décrit comment installer et configurer le plugin d'administration dans votre application.

### Configuration serveur

Déclaration de la dépendance dans le ```pom.xml``` du projet
```xml
<dependency>
    <groupId>net.codjo.administration</groupId>
    <artifactId>codjo-administration-server</artifactId>
</dependency>
```
Installation du plugin
```java
import net.codjo.administration.server.plugin.AdministrationServerPlugin;
...
ServerCore server = new ServerCore();
...
server.addPlugin(AdministrationServerPlugin.class);
...
```

### Configuration IHM

Déclaration de la dépendance dans le ```pom.xml``` du projet
```xml
<dependency>
    <groupId>net.codjo.administration</groupId>
    <artifactId>codjo-administration-gui</artifactId>
</dependency>
```
Installation du plugin
```java
import net.codjo.administration.gui.plugin.AdministrationGuiPlugin;
...
MadGuiCore gui = new MadGuiCore();
...
gui.addPlugin(AdministrationGuiPlugin.class);
...
```
Il suffit d'ajouter les actions suivantes dans votre menu.
```xml
<menu plugin="net.codjo.administration.gui.plugin.AdministrationGuiPlugin" actionId="AdministrationAction"/>
```
<u>NB</u> : Les utilisateurs devant administrer les plugins doivent posséder un rôle ayant la fonction ```administrate-server``` (cf. [security in codjo-administration]).
