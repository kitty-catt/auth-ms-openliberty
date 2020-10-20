## Keystore creation

```
keytool -genkeypair -dname "cn=bc.ibm.com, o=User, ou=IBM, c=US" -alias bckey -keyalg RSA -keysize 2048 -keypass password -storetype PKCS12 -keystore ./BCKeyStoreFile.p12 -storepass password -validity 3650
keytool -list -keystore ./BCKeyStoreFile.p12 -storepass password
keytool -export -alias bckey -file client.cer -keystore ./BCKeyStoreFile.p12 -storepass password
keytool -import -v -trustcacerts -alias bckey -file client.cer -keystore ./truststore.p12 -storepass password -noprompt
```

- Save `./BCKeyStoreFile.p12` under `src/main/liberty/config/resources/security`
