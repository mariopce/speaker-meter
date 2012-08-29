Feature: Vote feature

 Scenario: I go to voting screen
    Given I wait for 4 seconds
    Then I press list item number 1
    Then I wait upto 3 seconds for the "VoteActivity" screen to appear 
    Then I should see "Marek Defeicński"
    Then I should see "Calabash - BDD w Androidzie"
    Then I should see "Vote up"
    Then I should see "Vote down"

 Scenario: I can vote and see progress bar
	Given I wait for 3 seconds
	Then I press "Mariusz Saramak"
	Then I wait upto 3 seconds for the "VoteActivity" screen to appear
	Then I should see "Mariusz Saramak"
	Then I should see "Continuous Integration w Androidzie"
	When I press view with id "textViewUp"
	Then I wait for progress
	Then I should see "You voted Mariusz Saramak up"
	Then I wait for dialog to close
