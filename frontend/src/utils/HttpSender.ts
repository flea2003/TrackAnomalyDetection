/**
 * Utility class that handles request sending to the backend
 */
class HttpSender {
    
    private url: string = 'http://localhost:8080';

    /**
     * Send an asynchronous GET request to the backend server
     * mentioning the desired endpoint
     * @param endpoint - endpoint accessed
     */
    async get(endpoint: string) : Promise<any> {
        try{
            const response = await fetch(this.url + endpoint);
            if(!response.ok){
                throw new Error("Internal server error");
            }
            return await response.json();
        }
        // Error handling
        // TODO: Implement a smarter way to handle errors
        catch (error){
            console.log("Error: "+error);
        }
    }
}

export default HttpSender;