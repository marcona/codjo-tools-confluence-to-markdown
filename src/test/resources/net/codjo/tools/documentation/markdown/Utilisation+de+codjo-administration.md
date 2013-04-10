## Description

Cette page décrit les différentes fonctionnalités du plugin d'administration. 


## Fonctionnalités

* Arrêt/relance des plugins du serveur,
* Visualisation des logs,
* Audit sur les handlers et/ou la mémoire.

## Mise en place rapide

# Activation de l'audit des handlers
```java
public MyApplicationServerPlugin(AdministrationServerPlugin administration) {
    administration.getConfiguration().setRecordHandlerStatistics(true);
}
```
# Activation de l'audit de la mémoire
```java
public MyApplicationServerPlugin(AdministrationServerPlugin administration) {
    administration.getConfiguration().recordMemoryUsage(true);
}
```
# Paramétrage du répertoire de log
```xml|title=idea.xml
<execConfiguration>
    <name>Serveur</name>
    <description>Lance le serveur</description>
    <execClass>${serverMainClass}</execClass>
    <vmParameter>-Dlog.dir="c:/dev/user/temp" -Dlog4j.configuration="file:${user.home}/log4j.properties"</vmParameter>
    ...
</execConfiguration>
```


## Configuration avancée

### Répertoire de log
Un répertoire de log doit être spécifié pour pouvoir visualiser les logs du serveur.
De plus, les différents audits sont regroupés dans un fichier ```audit.log``` dans ce même répertoire.

Ce répertoire peut-être spécifié de différentes manières, par priorité décroissante :
* Via le server-config.properties en ajoutant la propriété suivante :
```
AdministrationService.auditDestinationDir = ${log.dir}
```
* Via la configuration ```AdministrationServerConfiguration``` :
```java
public MyApplicationServerPlugin(AdministrationServerPlugin administration) {
    administration.getConfiguration().setAuditDestinationDir("c:\\dev\\user\\temp");
}
```
* Via l'argument JVM ```-Dlog.dir=...``` (ce paramètre est déjà spécifié dans les scripts de démarrage des serveurs Unix)
* Via l'IHM dans l'onglet "Pilotage".

### Audit

Les différents audits peuvent être activés/désactivés dynamiquement via l'IHM du plugin d'administration mais aussi de façon statique comme décrit ci-dessous.

#### Audit des handlers
Le plugin ```AdministrationServerPlugin``` offre la possibilité de configurer l'audit des ```handlers```.

Cette configuration est rendue possible des trois manières suivantes :
* server-config.properties :
```xml
AdministrationService.recordHandlerStatistics = true
```

* AdministrationServerConfiguration :
```java
public MyApplicationServerPlugin(AdministrationServerPlugin administration) {
    administration.getConfiguration().setRecordHandlerStatistics(true);
}
```
* Via l'IHM dans l'onglet "Pilotage".

#### Audit de la mémoire
La lib ```codjo-administration``` permet de sauvegarder périodiquement les statistiques d'utilisation mémoire.
Par défaut, un relevé est fait toutes les 5 min. Ce relevé comprend la mémoire utilisée ainsi que la mémoire totale.

Cet audit est désactivé par défaut. L'activation peut-être faite des trois manières suivantes :
* server-config.properties :
```xml
AdministrationService.recordMemoryUsage = true
```

* AdministrationServerConfiguration :
```java
public MyApplicationServerPlugin(AdministrationServerPlugin administration) {
    administration.getConfiguration().recordMemoryUsage(true);
}
```
* Via l'IHM dans l'onglet "Pilotage".




