/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package passwordcrackingslave;


/**
 *
 * @author Artjom
 */
public class SlaveClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)   {
        System.out.println(hello("world"));
    }

    private static String hello(java.lang.String name) {
        master.MasterWebService_Service service = new master.MasterWebService_Service();
        master.MasterWebService port = service.getMasterWebServicePort();
        return port.hello(name);
    }

    
}
