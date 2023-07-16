package com.sdc.vault.state;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

public class CredentialsManager {
    // TODO: We might want to namespace this to the username so that
    // we can store multiple usernames per vault host
    // e.g. one user might have multiple logins for a given vault host or okta host?
    // how do make sure we auto populate the last value used? We don't want to force the user
    // to choose if there are multiple or hae to input a username to look up the password / token

    private static final String subSystem = "com.sdc.vault";
    private static final CredentialAttributes oktaCredentialAttributes = new CredentialAttributes(CredentialAttributesKt.generateServiceName(subSystem, "okta"));

    public static void setOktaCredentials(@Nullable final String username, @Nullable final String password) {
        PasswordSafe.getInstance().set(oktaCredentialAttributes, new Credentials(username, password));
    }

    @NotNull
    public static String getOKTAUsername() {
        final Credentials credentials = PasswordSafe.getInstance().get(oktaCredentialAttributes);
        return (credentials != null && credentials.getUserName() != null) ? credentials.getUserName() : "";
    }

    @NotNull
    public static String getOKTAPassword() {
        final String password = PasswordSafe.getInstance().getPassword(oktaCredentialAttributes);
        return (password == null) ? "" : password;
    }

    public static void setVaultToken(final URI vaultAddress, final String token) {
        final CredentialAttributes credentialAttributes = new CredentialAttributes(CredentialAttributesKt.generateServiceName(subSystem, vaultAddress.getHost()));
        PasswordSafe.getInstance().set(credentialAttributes, new Credentials(vaultAddress.getHost(), token));
    }

    public static String getVaultToken(final URI vaultAddress) {
        // TODO: Vault tokens need to be associated to the username that was used to fetch and set them
        // vault does have a display name that serves this purpose. We could look it up everytime
        //     "display_name": "okta-spencerdcarlson@gmail.com". Calling getinfo for a token gives us that
        // We can see that a vault token came from okta for username "spencer". I am guessing that
        // okta sets that to vault after oauth, so it could be different for other oatuh providers (google)
        final CredentialAttributes credentialAttributes = new CredentialAttributes(CredentialAttributesKt.generateServiceName(subSystem, vaultAddress.getHost()));
        final String token = PasswordSafe.getInstance().getPassword(credentialAttributes);
        return (token == null) ? "" : token;
    }
}
