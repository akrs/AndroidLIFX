package me.akrs.AndroidLIFX.testing;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

import me.akrs.AndroidLIFX.Discoverer;
import me.akrs.AndroidLIFX.utils.Logger;

public class TestCMD {
	public static void main (String[] args) {
		Enumeration<NetworkInterface> interfaces = null;
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e1) {
			Logger.log("unable to get all NetworkInterfaces", e1);
		}
		ArrayList<NetworkInterface> ifaces = Collections.list(interfaces);
		
		int i = 0;
		Scanner s = new Scanner(System.in);
		System.out.print("Please choose a network interface:\n-----------------------------------------\n");
		for (NetworkInterface iface : ifaces) {
			System.out.println(i + ") " + iface.getDisplayName());
			i++;
		}
		
		int j = s.nextInt();
		s.close();
		
		List<InterfaceAddress> addresses = ifaces.get(j).getInterfaceAddresses();
		InetAddress broadcast = null;
		
		for (InterfaceAddress address : addresses) {
			if (address != null) {
				Logger.log("Found address: " + address + " With broadcast address: " + address.getBroadcast(), Logger.DEBUG);
				if (address.getBroadcast() != null) {
					broadcast = address.getBroadcast();
				}
			}
		}
		
		Discoverer d = new Discoverer(broadcast);
		
		d.startSearch();
		
		while (d.getNetwork().getNumberOfBulbs() == 0) {
			continue;
		}
		
		System.out.println("We got a network.");
	}
}
