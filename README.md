<h3>How to run the Corona App</h3>

In this order: 
1. Run the RegistarScreen to start the Registrar
2. Run the MatchingScreen to start the Matching Service
3. Run the MixingProxyScreen to start the Mixing Proxy

Then (no order):
- Run VisitorScreen to create Visitors accounts
- Run CatheringScreen to create Cathering accounts
- Run Doctor to create Doctor
- Run Inspector to create Inspector

<h3>Information about the Corona app </h3>

At the registrar the visitors are shown on the left and the catherings on the right in two ListViews. There is a 'New Day' button to demonstrate a new day. 

The mixing proxy has a Listview of all capsules it has received in the current time interval. After each time interval the capsules are sent to the matching service. The button 'flush' is used to indicate the end of a time interval (1 hour). 

The matching service simply has a list view of all received capsules. 

The visitor starts creating a profile by entering his name and cell phone number. Then there is a menu to first visit a cathering facility with the button visit. There the visitor is asked to scan a QR-code (in this case it can be entered in the form of a string), if this is approved the user gets a unique figure and the possibility to leave the facility. Then the menu also has a button to generate the logs of the last 7 days as QR-code. These are then printed in the command line. If the visitor came close to an infected person, they will also receive a message in command line. 

The cathering facility gets a menu where he can enter his name, his unique business number and his location. Their daily QR-code is always displayed in the command line. 

The inspector has the possibility to enter a QR-code in the form of a string and the inspector will get a notification whether it is a real or a fake QR-code. 

Finally, the doctor has the possibility to enter the logs of a user by scanning a QR-code. In our prototype this is done by entering a string. The doctor can then indicate whether the person has been tested positive or negative.

