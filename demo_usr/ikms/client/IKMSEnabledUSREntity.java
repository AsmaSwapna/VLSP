package demo_usr.ikms.client;

import ikms.client.IKMSClientRestListener;
import ikms.client.IKMSEnabledEntity;
import us.monoid.json.JSONObject;
import demo_usr.ikms.TFTP.RestOverTFTPClient;
import demo_usr.ikms.client.utils.Logging;

public class IKMSEnabledUSREntity extends IKMSEnabledEntity{

	// InformationManagementInterface which talks to the IKMS
	protected InformationManagementInterface informationManagementInterface;

	// InformationExchangeInterface which talks to the IKMS
	protected InformationExchangeInterface informationExchangeInterface;

	// for distributed virtual infrastructure deployment: the virtual machine address hosting the knowledge forwarder
	protected String ikmsForwarderHost = null;

	// semaphores for running state
	protected boolean running = true;
	protected boolean stopRunning = false;

	// the tftp client (used for distributed virtual infrastructure deployment)
	RestOverTFTPClient tftpClient=null;

	// Initializes and registers entity with the IKMS
	protected void initializeAndRegister(JSONObject registrationInfo) {

		// if restPort is not set, use entityid as port
		if (entityPort==0)
			entityPort = entityid;

		Logging.Log(entityid, "Running rest listener:"+entityPort);
		// sets up Rest Listener for callbacks (i.e., for information subscribe or information flow negotiation updates)
		restListener = new IKMSClientRestListener(this, entityPort);
		restListener.start();

		// Allocate InformationManagementInterface & InformationExchangeInterface
		if (registrationInfo==null) {
			// it is an IKMS client, do not register
		} else {
			// Allocate IKMS client interfaces
			int ikmsForwarderPort=0;

			ikmsForwarderPort = 20000 + Integer.valueOf(ikmsForwarderHost);
			tftpClient = new RestOverTFTPClient (ikmsForwarderHost, ikmsForwarderPort);
			Logging.Log(entityid, "Connecting with IKMS host:"+ikmsForwarderHost+":"+ikmsForwarderPort);
			informationManagementInterface = new InformationManagementInterface(ikmsHost, String.valueOf(ikmsPort), tftpClient);
			informationExchangeInterface = new InformationExchangeInterface(ikmsHost, String.valueOf(ikmsPort), tftpClient);			

			// Entity Registration example
			while (true) {
				// check if can talk to IKMS
				Logging.Log(entityid, "Registering entityid:"+entityid);
				if (registerWithIKMS(entityid, registrationInfo)) {
					Logging.Log(entityid, "Make connection with IKMS");
					break;
				} else {
					Logging.Log(entityid, "Cannot interact with IKMS- retrying after 5000 ms");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}
				}
			}
		}
	}

	// shutting down the entity
	public void shutDown() {
		// stop running
		restListener.stop();
		// stop tftp client ?
		//if (tftpClient!=null)
		//tftpClient.
	}	
}