# Plugin Development


## Enable Debug Logs
### Open Debug Log Settings
* *Help > Diagnostic Tools > Debug Log Settings...*
* Input class names to enable 
    * Preface package with `#`
    * Append `:trace` to class name to enable trace level (warning trace level logging emits credentials)
    * Example  
        * ```
            #com.sdc.vault.VaultService:trace
            #com.sdc.vault.client.VaultCLIClient:trace
            #com.sdc.vault.client.VaultHTTPClient:trace
            #com.sdc.vault.ui.VaultAuthInterceptor:trace
          ```
* Find Log File: *Help > Show Log in Finder*

### Screenshot Examples
![Debug Log Settings](./img/Debug%20Log%20Settings.png)
![Configure](./img/Configure%20Debug%20Logs.png)

### Plugins
* Vault
  * [marketplace](https://plugins.jetbrains.com/plugin/16828-vault)
  * [source](https://github.com/codecacti/intellij-vault-plugin)
* postgresql-vault-auth (⎇)
  * [marketplace](https://plugins.jetbrains.com/plugin/16104-postgresql-vault-auth)
  * [source](https://github.com/davidsteinsland/postgres-vault-auth)
* Datagrip-Vault (Popular)
  * [marketplace](https://plugins.jetbrains.com/plugin/18522-datagrip-vault)
  * [source](https://github.com/premium-minds/datagrip-vault-plugin)