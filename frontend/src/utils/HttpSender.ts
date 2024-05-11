class HttpSender {
    
    private url: string = 'http://localhost:8080';

    // Method to send a GET request to the server
    async get(endpoint: string) : Promise<any> {
        try{
            const response = await fetch(this.url + endpoint);
            if(!response.ok){
                throw new Error("Internal server error");
            }
            return await response.json();
        }
        // Error handling
        catch (error){
            console.log("Error: "+error);
        }
    }
}

export default HttpSender;