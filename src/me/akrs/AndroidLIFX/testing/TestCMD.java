package me.akrs.AndroidLIFX.testing;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

import me.akrs.AndroidLIFX.network.Bulb;
import me.akrs.AndroidLIFX.network.BulbNetwork;
import me.akrs.AndroidLIFX.utils.java.Discoverer;
import me.akrs.AndroidLIFX.utils.java.Logger;

public class TestCMD {
	public static void main (String[] args) throws IOException {
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
		
		BulbNetwork net = d.getNetwork();
		
		while (d.getNetwork().getNumberOfBulbs() != 2) {
			continue;
		}
		
		System.out.print("We got a network.\n" + net.toString() + "\nChoose a bulb, or 0 for network:\n");
		d.stopSearch();
		
		int i1 = 0;
		List<Bulb> bulbs = net.getBulbList();
		for (Bulb b : bulbs) {
			System.out.println(++i1 +") " + b.toString());
		}
		int index = s.nextInt() - 1;
		if (index == -1) {
			boolean bob = true;
			while (bob) {
				System.out.println("Choose a command:\n1) Off\n2) On\n3) Hue");
				
				j = s.nextInt();
				switch (j) {
				case 0:
					bob = false;
				case 1:
					net.off();
					break;
				case 2:
					net.on();
					break;
				default:
					bob = false;
					break;
				}
			}
		} else {
			Bulb b = net.getBulbById(index);
			boolean bob = true;
			while (bob) {
				System.out.println("Choose a command:\n1) Off\n2) On\n3) Hue");
				
				j = s.nextInt();
				switch (j) {
				case 0:
					bob = false;
				case 1:
					b.off();
					break;
				case 2:
					b.on();
					break;
				case 3:
					System.out.print("Enter a hue: ");
					b.setHue(s.nextShort());
					break;
				default:
					break;
				}
			}
		}
		s.close();
		net.close();
		return;
	}
}
