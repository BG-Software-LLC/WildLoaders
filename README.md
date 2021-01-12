# WildLoaders

WildLoaders - The best alternative for alts!

## Compiling

You can compile the project using gradlew.<br>
Run `gradlew shadowJar build` in console to build the project.<br>
You can find already compiled jars on our [Jenkins](https://hub.bg-software.com/) hub!<br>
You must add yourself all the private jars or purchase access to our private repository.

##### Private Jars:
- EpicSpawners by Songoda [[link]](https://songoda.com/marketplace/product/13)

## API

You can hook into the plugin by using the built-in API module.<br>
The API module is safe to be used, its methods will not be renamed or changed, and will not have methods removed 
without any further warning.<br>
You can add the API as a dependency using Maven or Gradle:<br>

#### Maven
```
<repository>
    <id>bg-repo</id>
    <url>https://repo.bg-software.com/repository/api/</url>
</repository>

<dependency>
    <groupId>com.bgsoftware</groupId>
    <artifactId>WildLoadersAPI</artifactId>
    <version>latest</version>
</dependency>
```

#### Gradle
```
repositories {
    maven { url 'https://repo.bg-software.com/repository/api/' }
}

dependencies {
    compileOnly 'com.bgsoftware:WildLoadersAPI:latest'
}
```

## Updates

This plugin is provided "as is", which means no updates or new features are guaranteed. We will do our best to keep 
updating and pushing new updates, and you are more than welcome to contribute your time as well and make pull requests
for bug fixes. 

## License

This plugin is licensed under GNU GPL v3.0
