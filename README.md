This Expense Tracker was a small project I built to explore and learn different features in the Android Jetpack library. The idea came to me as I began my exchange study term in Sweden, and I thought it would be useful to have an app that could store my expenses and quickly display the total costs converted back to my home currency.

The app allows you to add expenses, search for them either by category or by their title, and you can also easily convert the currency you purchased it in into your home currency that you decide in the settings with the click of a simple button.

<p align="center">
  <img src="https://github.com/varuhn36/ExpenseTracker/blob/main/Images/Homepage.jpg" alt="Homepage" width="140" height="280"/>
  <img src="https://github.com/varuhn36/ExpenseTracker/blob/main/Images/CategoryExpenses.jpg" alt="Category Expenses" width="140" height="280"/>
  <img src="https://github.com/varuhn36/ExpenseTracker/blob/main/Images/AddExpense.jpg" alt="Add Expense" width="140" height="280"/>
  <img src="https://github.com/varuhn36/ExpenseTracker/blob/main/Images/ExpenseMenuDropDown.jpg" alt="Expense Menu Drop Down" width="140" height="280"/>
  <img src="https://github.com/varuhn36/ExpenseTracker/blob/main/Images/EditExpense.jpg" alt="Edit Expense" width="140" height="280"/>
  <img src="https://github.com/varuhn36/ExpenseTracker/blob/main/Images/ExpenseDetails.jpg" alt="Expense Details" width="140" height="280"/>
  <img src="https://github.com/varuhn36/ExpenseTracker/blob/main/Images/Settings.jpg" alt="Settings" width="140" height="280"/>
</p>

Technologies Used:

- Jetpack Compose UI
- Room DB
- Hilt
- Retrofit
- [Frankfurter currency API](https://frankfurter.dev/)
- Datastore to persist user preferences (Just their home currency for now)

In the future, I plan to extend this project to allow users to share expenses and collaborate, which could be especially useful for planning group trips.
