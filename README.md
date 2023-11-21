Go4Lunch Application Specification Document

Overview
Go4Lunch is designed to be a collaborative application for employees to find and select restaurants in their vicinity and share their choices with colleagues. It facilitates group lunch arrangements by allowing users to see where their peers are dining and join them. Prior to lunchtime, the app sends notifications to remind employees to meet their colleagues.

Back-end Integration
The mobile application leverages Firebase as its back-end service to manage user accounts, authentication through third-party services like Facebook and Google, data storage, and push notifications. Users must have a Google account to access Firebase functionalities and integrate Firebase dependencies into their Android Studio project.

Authentication
Access to the application requires login using a Google or Facebook account. This step is crucial to ensure user identity verification and prevent impersonation.

Main Views
Go4Lunch features three primary views accessible via three buttons at the bottom of the screen:

Map view of restaurants
List view of restaurants
View of colleagues using the app
By default, the map view of restaurants is displayed upon user login.

Map View
The app automatically geolocates the user, displaying nearby restaurants with custom pins on the map. Restaurants chosen by colleagues are highlighted with green pins. Users can tap on a pin to view the restaurant's detailed information.

List View
This view provides detailed information about the restaurants on the map, including:

Restaurant name
Distance from the user
Restaurant image (if available)
Type of restaurant (optional)
Address
Number of interested colleagues
Opening hours
Number of positive reviews (0 to 3 stars)
Detailed Restaurant Information
When a user selects a restaurant, they can view detailed information, including:

A button to indicate their restaurant choice
A call button to phone the restaurant (if available)
A like button to express preference (stored on Firebase)
A website button to visit the restaurant's site (if available)
A list of colleagues planning to dine at the restaurant (displayed only if there are any)
Colleagues List
Displays all colleagues and their restaurant choices, with an option to view the detailed page of the selected restaurant.

Search Functionality
A search icon on each view allows for contextual search, updating the corresponding view with the search results based on restaurant names.

Menu
The menu button reveals a sidebar with profile information, a button to show the chosen restaurant, settings access, and a logout option.

Notifications
A notification is sent to users who have selected a restaurant, reminding them of their choice, address, and the colleagues joining them.

Translation
The application must offer at least French and English versions to cater to international colleagues.

Additional Feature
Developers can choose to add a chat feature, integrate Twitter authentication, or implement sorting criteria for restaurants.

Constraints
The application is to be developed using Java.

Application Screenshots
The document includes screenshots illustrating the application interface at different stages.

<img width="209" alt="image" src="https://github.com/CedricHaegele/OC_P7_Go4Lunch/assets/85683236/96dec6c5-325c-49ec-a152-4e82eaebb5a8">
<img width="208" alt="image" src="https://github.com/CedricHaegele/OC_P7_Go4Lunch/assets/85683236/d95c07c2-3c60-4497-b7c7-268ebcf927af">
<img width="207" alt="image" src="https://github.com/CedricHaegele/OC_P7_Go4Lunch/assets/85683236/87db2227-8c29-4f05-8f09-ead517d37f7f">
<img width="217" alt="image" src="https://github.com/CedricHaegele/OC_P7_Go4Lunch/assets/85683236/ff4d18ac-1343-41a5-9963-205b82ce5633">
<img width="215" alt="image" src="https://github.com/CedricHaegele/OC_P7_Go4Lunch/assets/85683236/5b2bae47-e43b-40b0-84c3-b77ae4272249">
<img width="212" alt="image" src="https://github.com/CedricHaegele/OC_P7_Go4Lunch/assets/85683236/0dd90e03-899d-474d-b0b9-1b4bbebeed37">






