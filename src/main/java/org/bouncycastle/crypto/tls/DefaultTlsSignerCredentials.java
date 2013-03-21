package org.bouncycastle.crypto.tls;

import java.io.IOException;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;

public class DefaultTlsSignerCredentials implements TlsSignerCredentials
{
    protected TlsContext context;
    protected Certificate clientCert;
    protected AsymmetricKeyParameter clientPrivateKey;

    protected TlsSigner clientSigner;

    public DefaultTlsSignerCredentials(TlsContext context, Certificate clientCertificate,
        AsymmetricKeyParameter clientPrivateKey)
    {
        if (clientCertificate == null)
        {
            throw new IllegalArgumentException("'clientCertificate' cannot be null");
        }
        if (clientCertificate.isEmpty())
        {
            throw new IllegalArgumentException("'clientCertificate' cannot be empty");
        }
        if (clientPrivateKey == null)
        {
            throw new IllegalArgumentException("'clientPrivateKey' cannot be null");
        }
        if (!clientPrivateKey.isPrivate())
        {
            throw new IllegalArgumentException("'clientPrivateKey' must be private");
        }

        if (clientPrivateKey instanceof RSAKeyParameters)
        {
            clientSigner = new TlsRSASigner();
        }
        else if (clientPrivateKey instanceof DSAPrivateKeyParameters)
        {
            clientSigner = new TlsDSSSigner();
        }
        else if (clientPrivateKey instanceof ECPrivateKeyParameters)
        {
            clientSigner = new TlsECDSASigner();
        }
        else
        {
            throw new IllegalArgumentException("'clientPrivateKey' type not supported: "
                + clientPrivateKey.getClass().getName());
        }

        this.context = context;
        this.clientCert = clientCertificate;
        this.clientPrivateKey = clientPrivateKey;
    }

    public Certificate getCertificate()
    {
        return clientCert;
    }

    public byte[] generateCertificateSignature(byte[] md5andsha1) throws IOException
    {
        try
        {
            return clientSigner.calculateRawSignature(context.getSecureRandom(), clientPrivateKey,
                md5andsha1);
        }
        catch (CryptoException e)
        {
            throw new TlsFatalAlert(AlertDescription.internal_error);
        }
    }
}
