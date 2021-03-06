package org.yawlfoundation.yawl.engine;

import org.yawlfoundation.yawl.engine.interfce.ServletUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gary on 16-8-7.
 */
public class Engine_Client  {

    // allows the prevention of socket reads from blocking indefinitely
    private static int READ_TIMEOUT = 0;              // default: wait indefinitely



    /**
     * Executes a HTTP POST request on the url specified.
     *
     * @param urlStr the URL to send the POST to
     * @param paramsMap a set of attribute-value pairs that make up the posted data
     * @return the result of the POST request
     * @throws IOException when there's some kind of communication problem
     */
    protected String executePost(String urlStr, Map<String, String> paramsMap)
            throws IOException {


        return send(urlStr, paramsMap, true);
    }


    /**
     * Executes a rerouted HTTP GET request as a POST on the specified URL
     *
     * @param urlStr the URL to send the GET to
     * @param paramsMap a set of attribute-value pairs that make up the posted data
     * @return the result of the request
     * @throws IOException when there's some kind of communication problem
     */
    protected String executeGet(String urlStr, Map<String, String> paramsMap)
            throws IOException {

        return send(urlStr, paramsMap, false);
    }


    /**
     * Initialises a map for transporting parameters - used by extending classes
     * @param action the name of the action to take
     * @param handle the current engine session handle
     * @return the initialised Map
     */
    protected Map<String, String> prepareParamMap(String action, String handle) {
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("action", action) ;
        if (handle != null) paramMap.put("sessionHandle", handle) ;
        return paramMap;
    }


    /**
     * Set the read timeout value for future connections
     * @param timeout the timeout value in milliseconds. A value of -1 (the default)
     *                means a read will wait indefinitely.
     */
    protected void setReadTimeout(int timeout) {
        READ_TIMEOUT = timeout;
    }


    /**
     * Removes the outermost set of xml tags from a string, if any
     * @param xml the xml string to strip
     * @return the stripped xml string
     */
    protected String stripOuterElement(String xml) {
        if (xml != null) {
            int start = xml.indexOf('>') + 1;
            int end = xml.lastIndexOf('<');
            if (end > start) {
                return xml.substring(start, end);
            }
        }
        return xml;
    }


    /**
     * Sends data to the specified url via a HTTP POST, and returns the reply
     * @param connection the http url connection to send the request to
     * @param paramsMap a map of attribute=value pairs representing the data to send
     * @param stripOuterXML true if this was originally a POST request, false if a GET request
     * @return the response from the url
     * @throws IOException when there's some kind of communication problem
     */
    protected String send(HttpURLConnection connection, Map<String, String> paramsMap,
                          boolean stripOuterXML) throws IOException {

        // encode data and send query
        sendData(connection, encodeData(paramsMap)) ;

        //retrieve reply
        String result = getReply(connection.getInputStream());
        connection.disconnect();

        if (stripOuterXML) result = stripOuterElement(result);
        return result;

    }

    public String send(String urlStr,String data) throws IOException {
        HttpURLConnection connection=initPostConnection(urlStr);
        sendData(connection,data);
        String result=getReply(connection.getInputStream());
        connection.disconnect();

        return result;
    }


    /**
     * Initialises a HTTP POST connection
     * @param urlStr the url to connect to
     * @return an initialised POST connection
     * @throws IOException when there's some kind of communication problem
     */
    protected HttpURLConnection initPostConnection(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setReadTimeout(READ_TIMEOUT);

        // required to ensure the connection is not reused. When not set, spurious
        // intermittent problems (double posts, missing posts) occur under heavy load.
        connection.setRequestProperty("Connection", "close");
        return connection ;
    }


    /**
     * Tests a response message for success or failure
     * @param message the response message to test
     * @return true if the response represents success
     */
    public boolean successful(String message) {
        return (message != null)  &&
                (message.length() > 0) &&
                (! message.contains("<failure>")) ;
    }


    /*******************************************************************************/

    // PRIVATE METHODS //

    /**
     * Sends data to the specified url via a HTTP POST, and returns the reply
     * @param urlStr the url to connect to
     * @param paramsMap a map of attribute=value pairs representing the data to send
     * @param post true if this was originally a POST request, false if a GET request
     * @return the response from the url
     * @throws IOException when there's some kind of communication problem
     */
    private String send(String urlStr, Map<String, String> paramsMap, boolean post)
            throws IOException {
        return send(initPostConnection(urlStr), paramsMap, post);
    }




    /**
     * Encodes parameter values for HTTP transport
     * @param params a map of the data parameter values, of the form
     *        [param1=value1],[param2=value2]...
     * @return a formatted http data string with the data values encoded
     */
    private String encodeData(Map<String, String> params) {
        StringBuilder result = new StringBuilder("");
        for (String param : params.keySet()) {
            String value = params.get(param);
            if (value != null) {
                if (result.length() > 0) result.append("&");
                result.append(param)
                        .append("=")
                        .append(ServletUtils.urlEncode(value));
            }
        }
        return result.toString();
    }


    /**
     * Submits data on a HTTP connection
     * @param connection a valid, open HTTP connection
     * @param data the data to submit
     * @throws IOException when there's some kind of communication problem
     */
    private void sendData(HttpURLConnection connection, String data)
            throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
        out.write(data);
        out.close();
    }


    /**
     * Receives a reply from a HTTP submission
     * @param is the InputStream of a URL or Connection object
     * @return the stream's contents (ie. the HTTP reply)
     * @throws IOException when there's some kind of communication problem
     */
    private String getReply(InputStream is) throws IOException {
        final int BUF_SIZE = 16384;

        // read reply into a buffered byte stream - to preserve UTF-8
        BufferedInputStream inStream = new BufferedInputStream(is);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(BUF_SIZE);
        byte[] buffer = new byte[BUF_SIZE];

        // read chunks from the input stream and write them out
        int bytesRead;
        while ((bytesRead = inStream.read(buffer, 0, BUF_SIZE)) > 0) {
            outStream.write(buffer, 0, bytesRead);
        }

        outStream.close();
        inStream.close();

        // convert the bytes to a UTF-8 string
        return outStream.toString("UTF-8");
    }
}
