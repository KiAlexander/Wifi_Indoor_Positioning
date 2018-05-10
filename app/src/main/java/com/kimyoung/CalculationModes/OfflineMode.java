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

package com.kimyoung.CalculationModes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import android.os.Handler;
import com.kimyoung.Algorithms;
import com.kimyoung.LogRecord;
import com.kimyoung.RadioMap;

public class OfflineMode extends Thread {

	private String errMsg = null;
	private Handler handler;
	private final RadioMap RM;
	private final File test_data_file;
	private final int algorithm_selection;

	private double[] average_pos_err = new double[4];
	private double[] average_exe_time =new double[4];

	// The scan list to use for offline
	private ArrayList<LogRecord> OfflineScanList;

	public OfflineMode(RadioMap RM, File test_data_file, int algorithm_selection, Handler handler) {
		this.RM = RM;
		this.test_data_file = test_data_file;
		this.handler = handler;
		this.algorithm_selection = algorithm_selection;
		this.OfflineScanList = new ArrayList<LogRecord>();
	}

	public void run() {

		if (!test_data_file.isFile() || !test_data_file.exists() || !test_data_file.canRead()) {
			errMsg = test_data_file + " does not exist or is not readable";
			handler.sendEmptyMessage(-1);
			return;
		}

		OfflineScanList.clear();

		BufferedReader reader;
		String line;
		String[] temp;

		String[] test_geo = new String[4];
		int[] count_test_pos = new int[4];
		double[] pos_error = new double[4];
		double[] sum_pos_error = new double[4];

		long bytesRead = 0;
		long bytesTotal = test_data_file.length();
		int perc = 0;

		long start ;
		long finish ;
		long total[] = new long[4];

		ArrayList<String> MacAddressList = new ArrayList<String>();

		try {

			reader = new BufferedReader(new FileReader(test_data_file));

			/* Read the first line */
			line = reader.readLine();

			// Must exists
			if (line == null) {
				errMsg = test_data_file + " file is corrupted";
				handler.sendEmptyMessage(-1);
				return;
			}

			bytesRead += line.length() + 1;

			if (perc < (int) (((float) bytesRead / (float) bytesTotal) * 100)) {
				perc = (int) (((float) bytesRead / (float) bytesTotal) * 100);
				handler.sendEmptyMessage(perc);
			}

			/* Store the Mac Addresses */
			if (line.startsWith("#")) {
				line = line.replace(", ", " ");
				temp = line.split(" ");

				// Must have more than 4 fields
				if (temp.length < 4) {
					errMsg = test_data_file + " file is corrupted";
					handler.sendEmptyMessage(-1);
					return;
				}

				// Store all Mac Addresses
				for (int i = 3; i < temp.length; ++i)
					MacAddressList.add(temp[i]);
			} else {
				errMsg = test_data_file + " file is corrupted";
				handler.sendEmptyMessage(-1);
				return;
			}

				while ((line = reader.readLine()) != null) {

					bytesRead += line.length() + 1;

					line = line.trim().replace(", ", " ");
					temp = line.split(" ");

					if (temp.length < 3) {
						errMsg = test_data_file + " file is corrupted";
						handler.sendEmptyMessage(-1);
						return;
					}

					if (MacAddressList.size() != temp.length - 2) {
						errMsg = test_data_file + " file is corrupted";
						handler.sendEmptyMessage(-1);
						return;
					}

					for (int i = 2; i < temp.length; ++i) {
						LogRecord lr = new LogRecord(MacAddressList.get(i - 2), Integer.parseInt(temp[i]));
						OfflineScanList.add(lr);
					}

					if (perc < (int) (((float) bytesRead / (float) bytesTotal) * 100)) {
						perc = (int) (((float) bytesRead / (float) bytesTotal) * 100);
						handler.sendEmptyMessage(perc);
					}

					for(int a = 0;a < 4;a++) {

						start = System.currentTimeMillis();

						test_geo[a] = Algorithms.ProcessingAlgorithms(OfflineScanList, RM, a + 1);

						if (test_geo[a] == null) {
							errMsg = "Can't calculate a location. Check that test data and radio map files refer to the same area.";
							handler.sendEmptyMessage(-1);
							return;
						}

						finish = System.currentTimeMillis();

						total[a] += (finish - start);


//						OfflineScanList.clear();

						pos_error[a] = calculateEuclideanDistance(temp[0] + " " + temp[1], test_geo[a]);

						if (pos_error[a] != -1) {
							sum_pos_error[a] += pos_error[a];
							count_test_pos[a]++;
						}
					}
					OfflineScanList.clear();
				}

				reader.close();

				handler.sendEmptyMessage(100);

				for(int a = 0;a < 4;a++) {
					average_pos_err[a] = sum_pos_error[a] / (double) count_test_pos[a];
					average_exe_time[a] = total[a] / (double) count_test_pos[a];
				}

//			}

				handler.sendEmptyMessage(-1);
				errMsg = null;

		} catch (Exception ex) {
			errMsg = "Can't calculate a location.\nError: " + ex.getMessage() + "." + "\nCheck that test data and radio map files are not corrupted.";
			handler.sendEmptyMessage(-1);
		}
	}

	private double calculateEuclideanDistance(String real, String estimate) {

		double pos_error;
		String[] temp_real;
		String[] temp_estimate;
		double x1, x2;

		temp_real = real.split(" ");
		temp_estimate = estimate.split(" ");

		try {
			x1 = Math.pow((Double.parseDouble(temp_real[0]) - Double.parseDouble(temp_estimate[0])), 2);
			x2 = Math.pow((Double.parseDouble(temp_real[1]) - Double.parseDouble(temp_estimate[1])), 2);
		} catch (Exception e) {
			return -1;
		}

		pos_error = Math.sqrt((x1 + x2));

		return pos_error;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public double[] getAverage_pos_err() {
		return average_pos_err;
	}

	public double[] getAverage_exe_time() {
		return average_exe_time;
	}

}
