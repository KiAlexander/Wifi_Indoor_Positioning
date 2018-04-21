/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * Υou should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.kimyoung;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class PowerTutor {

	public static String getLastFilePowerTutor(String path) {
		File dir = new File(path);

		Long l = -1l;
		Long curLogTime;

		String[] children = dir.list();
		if (children == null) {
			return null;
		} else {
			for (int i = 0; i < children.length; i++) {
				// Get filename of file or directory
				if (children[i].contains("PowerTrace")) {
					curLogTime = Long.parseLong((String) children[i].subSequence(10, 23));
					if (curLogTime > l)
						l = curLogTime;
				}
			}
		}
		if (l == -1)
			return null;
		else
			return "PowerTrace" + l + ".log";

	}

	public static Power getPower(String file) {

		String strLine;
		String pID = null;
		int wifi = 0, cpu = 0;

		try {

			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			while ((strLine = br.readLine()) != null) {

				if (pID == null && strLine.contains("cy.com.findme")) {
					pID = getProcessId(strLine.split(" "));
					continue;
				}

				if (pID != null) {
					String[] str = strLine.split(" ");
					if (str.length == 2) {
						if (str[0].contains(pID) && str[0].contains("CPU-" + pID)) {
							cpu = cpu + Integer.parseInt(str[1]);
						}
						if (str[0].contains(String.valueOf(pID)) && str[0].contains("Wifi-" + pID)) {
							wifi = wifi + Integer.parseInt(str[1]);
						}
					}
				}
			}

			in.close();

		} catch (Exception e) {
			return null;

		}

		return new Power(cpu, wifi);

	}

	public static String getProcessId(String[] str) {

		if (str.length == 3) // org.com.clientpositioning
			return str[1];

		return null;
	}

}

class Power {

	int CPU;
	int WIFI;

	Power(int cpu, int wifi) {
		CPU = cpu;
		WIFI = wifi;
	}
}