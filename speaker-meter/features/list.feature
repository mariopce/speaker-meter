Feature: List feature

 Scenario: When I wait I see Speaker list
    Given I wait for 10 seconds
    Then I wait for the "SpeakerMeterActivity" screen to appear
    Then I should see "Marek Defeciński"
    Then I should see "Calabash - BDD w Androidzie"
    Then I should see "Mariusz Saramak"
    Then I should see "Continuous Integration w Androidzie"
    Then I should see "Paweł Włodarski"
    Then I should see "Psychologiczne podstawy jakości kodu"

 Scenario: When I click menu refresh I should see list update
	Given I wait for 3 seconds
	When I select "Refresh" from the menu
	Then I wait for progress
