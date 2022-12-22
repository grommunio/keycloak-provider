Build and Deploy the Quickstart
-------------------------------

You must first configure the datasource it uses. 
For that, copy the [conf/quarkus.properties](conf/quarkus.properties) to the `conf` directory of the server distribution.

To package the provider, run the following maven command:

   ````
   mvn package
   ````

To install the provider, copy the target/grommunio-user-storage.jar JAR file to the `providers` directory of the server distribution.

Finally, start the server as follows:

    kc.[sh|bat] start

Enable the Provider for a Realm
-------------------------------
Login to the <span>Keycloak</span> Admin Console and got to the User Federation tab.   You should now see your deployed provider in the add-provider list box.
Add the provider, save it.  This will now enable the provider for the 'master' realm.  Because this provider implements the UserRegistrationProvider interface, any new user you create in the
admin console or on the registration pages of <span>Keycloak</span>, will be created in the custom store used by the provider.  If you go
to the Users tab in the Admin Console and create a new user, you'll be able to see the provider in action.

