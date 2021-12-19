package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JMenu;

import burp.BurpExtender;
import burp.IHttpRequestResponse;
import burp.Methods;
import config.ConfigEntry;
import config.GUI;

/**
 *
 * @author bit4woo 
 */

public class Custom_System_Command_Menu extends JMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public BurpExtender myburp;
	public String[] Custom_System_Command_Menu;


	public Custom_System_Command_Menu(BurpExtender burp){
		try {
			this.setText("^_^ Custom System Command");
			this.myburp = burp;

			List<ConfigEntry> configs = GUI.tableModel.getConfigByType(ConfigEntry.Action_Run_System_Command);

			Iterator<ConfigEntry> it = configs.iterator();
			List<String> tmp = new ArrayList<String>();
			while (it.hasNext()) {
				ConfigEntry item = it.next();
				tmp.add(item.getKey());//custom payload name
			}

			Custom_System_Command_Menu = tmp.toArray(new String[0]);
			if (configs.size()<=5) {//
				
			}else {
				Methods.add_MenuItem_and_listener(this, Custom_System_Command_Menu, new CustomSystemCmdItemListener(myburp));
			}
		} catch (Exception e) {
			e.printStackTrace(BurpExtender.getStderr());
		}
	}
}

class CustomSystemCmdItemListener implements ActionListener {

	BurpExtender myburp;
	CustomSystemCmdItemListener(BurpExtender burp) {
		myburp = burp;
	}

	@Override
	public void actionPerformed(ActionEvent e) {


		IHttpRequestResponse req = myburp.invocation.getSelectedMessages()[0];
		byte[] request = req.getRequest();

		int[] selectedIndex = myburp.invocation.getSelectionBounds();

		String action = e.getActionCommand();

		byte[] newRequest = GetNewRequest(request, selectedIndex, action);
		req.setRequest(newRequest);
	}

	public byte[] GetNewRequest(byte[] request,int[] selectedIndex, String action){//action is the payload name

		//debug
		//PrintWriter stderr = new PrintWriter(myburp.callbacks.getStderr(), true);
		
		byte[] payloadBytes = null;
		String payload =GUI.tableModel.getConfigValueByKey(action);

		if (GUI.tableModel.getConfigTypeByKey(action).equals(ConfigEntry.Config_Custom_Payload)) {
			
			String host = myburp.invocation.getSelectedMessages()[0].getHttpService().getHost();


			if (payload.contains("%host")) {
				payload = payload.replaceAll("%host", host);
			}
			//debug
			//stderr.println(payload);

			if(payload.toLowerCase().contains("%dnslogserver")) {
				String dnslog = myburp.tableModel.getConfigValueByKey("DNSlogServer");
				Pattern p = Pattern.compile("(?i)%dnslogserver");
				Matcher m  = p.matcher(payload);

				while ( m.find() ) {
					String found = m.group(0);
					payload = payload.replaceAll(found, dnslog);
				}
			}
			//debug
			//stderr.println(payload);
			payloadBytes = payload.getBytes();
		}
		

		if (myburp.tableModel.getConfigTypeByKey(action).equals(ConfigEntry.Config_Custom_Payload_Base64)) {
			payloadBytes = Base64.getDecoder().decode(payload);
			//用IExtensionHelpers的stringToBytes bytesToString方法来转换的话？能保证准确性吗？
		}
		

		if(payloadBytes!=null) {
			return Methods.do_modify_request(request, selectedIndex, payloadBytes);
		}else {
			return request;
		}
	}
}
