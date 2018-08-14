import os
import io

class ADB:

	def __init__(self):
		self.listOfDevicesAttached = {};#listOfDevicesAttached[deviceID] = deviceState
		
	def checkAirplaneMode(self, deviceID):
		adbCommand       = 'adb -s ' + deviceID + ' shell settings get global airplane_mode_on';
		return self.sendADB(adbCommand, deviceID);
		
	def clearLogcat(self, deviceID):
		adbCommand       = 'adb -s ' + deviceID + ' logcat -b all -c';
		self.sendADB(adbCommand, deviceID);
		
	def getAttachedDevices(self):
		cmdAnswer = self.sendADB('adb devices');
		buffer    = io.StringIO(cmdAnswer);#convert cmdAnswer to string
		buffer.readline(); #ignores first line where the text is 'List of devices attached'
		answer    = buffer.readline();
		self.listOfDevicesAttached.clear();
		
		while( answer != '\n'):
			info                                = answer.split();
			model                               = self.getModel(info[0]);
			operationalSytem                    = self.getOS(info[0]);
			self.listOfDevicesAttached[info[0]] = [info[1], model, operationalSytem];#device ID = info[0], deviceState = info[1]
			answer                              = buffer.readline();
	
	def getCSC(self, deviceID):
		adbCommand       = 'adb -s ' + deviceID + ' shell getprop ril.official_cscver';
		return self.sendADB(adbCommand);
		
	def getModel(self, deviceID):
		adbCommand       = 'adb -s ' + deviceID + ' shell getprop ro.product.model';
		return self.sendADB(adbCommand);
		
	def getOS(self, deviceID):
		adbCommand       = 'adb -s ' + deviceID + ' shell getprop ro.build.version.release';
		return self.sendADB(adbCommand);
		
	def getPDA(self, deviceID):
		adbCommand       = 'adb -s ' + deviceID + ' shell getprop ro.build.PDA';
		return self.sendADB(adbCommand);
		
	def getSalesCode(self, deviceID):
		adbCommand       = 'adb -s ' + deviceID + ' shell getprop ro.csc.sales_code';
		return self.sendADB(adbCommand);
			
	def isDeviceConnected(self, deviceID):
		if( self.listOfDevicesAttached.get(deviceID) and self.listOfDevicesAttached[deviceID] != "unauthorized"):
			return True;
		return False;
					
	def printAttachedDevices(self):
		self.getAttachedDevices();
		
		if( len(self.listOfDevicesAttached) == 0 ):
			print('No devices attached');
		else:
			for device in self.listOfDevicesAttached:
				print  ( '************************************DEVICE************************************');
				print  ( 'Model            :' + self.listOfDevicesAttached.get(device)[1]);
				print  ( 'Operational Sytem:' + self.listOfDevicesAttached.get(device)[2]);
	
	def searchNetworks(self, deviceID):
		adbCommand       = 'adb -s ' + deviceID + ' logcat -b radio -d | find "QUERY_AVAILABLE_NETWORKS"';
		return self.sendADB(adbCommand);
		
	def searchNetworksResult(self, deviceID):
		adbCommand       = 'adb -s ' + deviceID + ' logcat -b radio -d | find "OPERATOR"';
		return self.sendADB(adbCommand);
		
	def selectDevice(self):
		self.getAttachedDevices();
		quantity = 0;
		
		if( len(self.listOfDevicesAttached) == 0 ):
			print('No devices attached');
			return None
		else:
			for device in self.listOfDevicesAttached:
				quantity = quantity + 1;
				print  ( '************************************DEVICE ' + str(quantity) + ' ************************************');
				print  ( 'Model            :' + self.listOfDevicesAttached.get(device)[1]);
				print  ( 'Operational Sytem:' + self.listOfDevicesAttached.get(device)[2]);
				
		print('\n')
		
		if(quantity > 1):
			answer = input('Type desired device by number (between 1 and ' + str(quantity) + ') : ');
			
			try:
				answer = int(answer);
			except ValueError:
				print('Incorrectly value entered. No devices selected.');
				return None;
			
			if(answer < 1 or answer > quantity):
				print('No device selected');
				return None;
			else:
				id = list( self.listOfDevicesAttached )[answer-1];
				print('Device selected: ' + self.listOfDevicesAttached[id][1] );
				return id;
		else:
			answer = input('Only device found: ' + self.listOfDevicesAttached[list(self.listOfDevicesAttached)[0]][1] + ', confirm ?  (Y/N): ');
			answer = str(answer);
			
			if(answer == 'Y' or answer == 'y'):
				id = list( self.listOfDevicesAttached )[0];
				print('Device selected: ' + self.listOfDevicesAttached[id][1]);
				return id;
			else:
				print('No device selected');
				return None
				
	def sendADB(self, command):
		answer = os.popen(command).read().replace('\r\n','');
		return answer;
		
	def sim(self, deviceID, mcc, mnc, msin, spn)
		adbCommand       = 'adb shell am start -n com.jose.myapplication/com.jose.myapplication.MainActivity';
		answer = os.popen(command).read().replace('\r\n','');
		adbCommand       = 'adb -s ' + deviceID + ' shell am broadcast -a SysmocomBroadcast --es mcc ' + mcc +' --es mnc ' + mnc+' --es msin ' + msin +' --es spn ' + spn;
		answer = os.popen(command).read().replace('\r\n','');
			
	def waitForDevice(self, deviceID):
		adbCommand       = 'adb -s ' + deviceID + ' wait-for-device';
		self.sendADB(adbCommand);
	
def main():
	adb = ADB();
	id = adb.selectDevice();	
	
	
	#ans = adb.checkAirplaneMode(id);
	#print(ans)
	if(id == None):
		print('None')
	else:
		print(id)
	
				
	
if __name__ == '__main__':
    main()
