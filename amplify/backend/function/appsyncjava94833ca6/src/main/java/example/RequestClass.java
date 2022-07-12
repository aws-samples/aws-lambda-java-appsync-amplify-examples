/* Amplify Params - DO NOT EDIT
	API_APPSYNCJAVA_GRAPHQLAPIENDPOINTOUTPUT
	API_APPSYNCJAVA_GRAPHQLAPIIDOUTPUT
	API_APPSYNCJAVA_GRAPHQLAPIKEYOUTPUT
	ENV
	REGION
Amplify Params - DO NOT EDIT */

package example;
        
     public class RequestClass {
        String firstName;
        String lastName;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public RequestClass(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public RequestClass() {
        }
    }