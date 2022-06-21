
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class main {
    public static void main(String[] args) {
        try{
            Socket s=new Socket("",1080);

            byte[] b1 = new byte[] {5, 2, 0, 2};
            s.getOutputStream().write(b1,0,4);

            byte[] r1 = new byte[2];
            s.getInputStream().read(r1,0,2);

            byte[] auth = authRequest("", "");
            s.getOutputStream().write(auth, 0, auth.length);

            byte[] r2 = new byte[2];
            s.getInputStream().read(r2, 0, 2);

            //byte[] b3 = new byte[]{5, 1, 0, 1, 	94, 100, (byte) 180, 70, (byte)((80 >> 8) & 0xff), (byte)(80 & 0xff)};
            byte[] b3 = hostConnectRequest("whatismyip.host", 443);
            s.getOutputStream().write(b3,0,b3.length);

            byte[] r3 = new byte[10];
            s.getInputStream().read(r3, 0, 10);

            SSLSocket sslSocket = (SSLSocket)  ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket(s,s.getInetAddress().getHostAddress(),s.getPort(),true);

            String req = "GET / HTTP/1.1\r\nHost: whatismyip.host\r\nUser-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.125 Safari/537.36\r\n\r\n";
            s.getOutputStream().write(req.getBytes(),0,req.length());

            byte[] resp = new byte[1000];
            s.getInputStream().read(resp, 0, 1000);

            System.out.println(new String(resp));


            s.close();
        }catch(Exception e){System.out.println(e);}
    }

    private static byte[] authRequest(String username, String password){
        byte[] auth = new byte[3 + username.length() + password.length()];
        auth[0] = 1;
        auth[1] = (byte) username.length();
        auth[2 + username.length()] = (byte) password.length();
        System.arraycopy(username.getBytes(), 0, auth, 2, username.length());
        System.arraycopy(password.getBytes(), 0, auth, 19, password.length());
        return auth;
    }

    private static byte[] hostConnectRequest(String host, int port) throws UnsupportedEncodingException, UnknownHostException {
        byte[] result;
        if(validIP(host)){
            byte[] ip = InetAddress.getByName(host).getAddress();
            result = new byte[10];
            result[0] = 5;
            result[1] = 1;
            result[2] = 0;
            result[3] = 1;
            result[8] = (byte)((port >> 8) & 0xff);
            result[9] = (byte)(port & 0xff);
            System.arraycopy(ip, 0, result, 4, ip.length);
        } else {
            result = new byte[7 + host.length()];
            result[0] = 5;
            result[1] = 1;
            result[2] = 0;
            result[3] = 3;
            result[4] = (byte) host.length();
            result[host.length()+5] = (byte)((port >> 8) & 0xff);
            result[host.length()+6] = (byte)(port & 0xff);
            System.arraycopy(host.getBytes("ISO-8859-1"), 0, result, 5, host.length());
        }
        return result;
    }

    public static boolean validIP (String ip) {
        try {
            if ( ip == null || ip.isEmpty() ) {
                return false;
            }

            String[] parts = ip.split( "\\." );
            if ( parts.length != 4 ) {
                return false;
            }

            for ( String s : parts ) {
                int i = Integer.parseInt( s );
                if ( (i < 0) || (i > 255) ) {
                    return false;
                }
            }
            if ( ip.endsWith(".") ) {
                return false;
            }

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}
