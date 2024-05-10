class HttpSender {
    
    private url: string = 'http://localhost:8080'; // TODO: Change this to the actual URL of the server

    // Method to send a GET request to the server
    async get(endpoint: string) : Promise<any> {
        const response = await fetch(this.url + endpoint);
        return await response.json();
    }
}

export default HttpSender;