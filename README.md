# The_Vivsionaries_LingoChat_App

Part 2:                                                                           
Victor Sumbo 		     PM ST10082741
Princess Muzanenhamo  ST10041651
Miguel Almeida        ST10025374

Youtube Link: https://www.youtube.com/watch?v=BEqKzEUYv-Q

Overview
LingoChat is a real-time messaging application designed to break down language barriers by translating messages instantly between users speaking different languages. The app enables users to communicate smoothly regardless of their preferred language, making interactions between users easier and more accessible.
This version (Part 2 of the project) includes the core functionalities such as user authentication, profile customization, and chat capabilities. However, the real-time translation feature, intended to translate messages on the go using Microsoft Translation APIs, which is fully functional. 
Key features 
1.	User Authentication
•	Users can sign up and log in using their phone number.
•	A One-Time Password (OTP) is sent via SMS to verify the user's phone number during registration or login. This ensures a secure and straightforward authentication process.
•	Once the OTP is verified, users can proceed to use the app, and they also have the option to use Single Sign-On (SSO) for subsequent logins through Google.

2.	User Profile customization”
•	Users can upload a profile picture and edit personal details such as phone numbers and usernames.
•	Customizable settings for notifications and privacy preferences are available.

3.	Real time Messaging:
•	The app supports real-time chat between users. The user interface allows for a smooth chat experience with push notifications to alert users about new messages.

4.	Language selection:
•	Users can select their preferred language for the app interface, supporting English and Afrikaans.
•	The system allows users to set a preferred language for incoming and outgoing messages in future updates.

5.	Database application
•	Firebase has been used to manage user data, chats, and other application states as required by Android Studio.
Technology stack:
•	Android Studio for the development environment.
•	Kotlin for app development.
•	Firebase for database management and real-time data synchronization.
•	Microsoft Translation APIs (planned) for language translation (instead of Google Cloud Services).
•	GitHub Actions
•	Version: Koala 2024.1.2

How to Use
1.	Sign In: Use the OTP sent to your phone number for secure registration or log in using Google SSO.
2.	Profile Setup: After signing in, customize your profile by uploading a photo and adjusting your personal details.
3.	Start a Chat: Navigate to the chat section, search for a contact, and start sending messages.
4.	Notifications: Customize notification settings in the app to suit your preferences.

Future updates
Future updates will focus on enhancing performance and expanding the language support beyond English and Afrikaans, integrating more advanced notification management features.
Video Demonstration
A video showcasing the app’s functionalities is available here: The video covers:
•	User sign-in via OTP and SSO.
•	Profile customization.
•	Real-time messaging.
•	Push notifications in action.
