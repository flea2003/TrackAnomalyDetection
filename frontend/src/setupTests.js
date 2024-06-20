if (!global.URL) {
  global.URL = {};
}

if (!global.URL.createObjectURL) {
  global.URL.createObjectURL = jest.fn(() => "http://dummyurl.com"); // Return a dummy URL or adjust as needed
}