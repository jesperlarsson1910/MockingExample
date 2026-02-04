## PaymentProcessor Refactoring Decisions

### API_KEY
Instead of having the key hardcoded and visible in the code we provide a config object that contains the key and could contain other information to expand the PaymentProcessor. Also makes for easier testing. 

### PaymentApi & PaymentApiResponse
Made response codes enum rather than a string, given that it can only be one of three options it lowers risk of errors.

Most transactions will either succeed or fail but can sometimes be pending. The response contains a payment ID and the status code provided by a financial institution in order to keep track of all transactions.

Throws error if something went wrong with the external API to indicate that the payment wasn't handled fully. We log these also so that we can troubleshoot or try again later if there is a temporary outage.


### PaymentRepo & Billable
Moving the DB logic to a repository is more flexible than having a hardcoded connection sending queries and also allows for easier testing.

Booking could implement Billable if we changed it to include price. We want to keep track of the price tied to any order and make sure that a customer can pay for their order rather than just generally give us money.


### EmailService
Expanded to include more relevant data to the customer

