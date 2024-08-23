# Configuration as Code Plugin - Groovy Scripting Extension

Extension for Jenkins Configuration-as-Code plugin that allows running Groovy scripts

## Up for adoption

This plugin is not being actively maintained. It is up for [adoption](https://www.jenkins.io/doc/developer/plugin-governance/adopt-a-plugin/).

It needs one or more additional maintainers that are actively using the plugin and are willing to keep it updated.  New maintainers should follow the [adopt a plugin instructions](https://www.jenkins.io/doc/developer/plugin-governance/adopt-a-plugin/) to request that they be added as maintainers.

## Run Groovy scripts

Configuration-as-Code can run Groovy scripts.

Groovy scripts can be:
 * inline, with entry `script`
 * from `url`
 * from local file, using `file`

### Sample configuration

```yaml
groovy:
  - script: >
      println("This is Groovy script!");
```



### Implementation notes

 * It is recommended to use semicolons at the end of lines
 * There is no dry run implemented for Groovy scripts feature
