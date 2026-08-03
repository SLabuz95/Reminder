# Introduction
An idea for the application was to send a SMS messages to customers of the hairdresser with an information about incoming appointment.

# Goal
Main goal is to simplify and automate the process of writing and sending a SMS message to selected customer. 

# Concept
## Version 1
### General
A user selects notification template, date and time of the appointment and the receiver of the notification (the customer).
If the user requests to send the notification,
the application generates a content of the notification based on a selected message template, date and a time of the appointment,
then send it to the selected receiver.

### Customer (receiver) selection
A user can:
- select the receiver from phone's contact list
- type the receiver's phone number

### Message template
A user manages message templates. A message template contains a content of the message, which will be sent. Additionally 
a message template can contain date or time tags. A user adds these tags to the content of the template message. The tags will be replaced with date or time specified by a user. 

### Success notification
A user will be notified with a toast notification if the notication was sent successfully or in case of any errors.

## Version 1.1
### General
The update provides a feature to send multiple notifications after selection of the message template.

When a user selects a message template, in the next activity the application requires to provide notification's data (date, time, contact/phone number).
The user can:
- add new notification data,
- edit or remove selected notification data,
- preview selected notification,
- accept to send multiple messages based on provided notification data's list
- cancel to return to activity with list of notification templates.

## Version 1.2
### General
The update:
- adds new activity to track progress of sending the notifications
- adds new activity which shows list of already sent notifications and their details (message content, errors, timestamp of sent confirmation)
