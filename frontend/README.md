# Frontend Documentation

In order to work with the frontend, one has to install the NodeJS programming environment. This can be done by running: 
```
sudo apt update
sudo apt install nodejs
node -v
```

This template project was bootstrapped with [Create React App](https://github.com/facebook/create-react-app).
The command that was used to generate it is: 
```
npx create-react-app frontend --template typescript --use-npm
```

`npm` - Node package manager.

`npx` - npm package runner.

`create-react-app` - command to initiate a react app.

`--template typescript` - flag to create a typescript template instead of javascript.

`--use-npm` - npx doesn't create a repo in the current folder.

## Description of the files

`tsconfig` - Typescript configuration.

`package.json` - the dependencies of the frontend.

`package-lock.json` - fetched versions of the dependencies.

`robots.txt` - a file used to communicate with web crawlers and other web robots about which parts of your site they are allowed to access.

## Available Scripts

In this directory, one can use the following commands:

`npm install` - installs dependencies listed in the `package.json` file.

`npm start` - Runs the app in the development mode. Open [http://localhost:3000](http://localhost:3000) to view it in the browser. The page will reload if you make edits. You will also see any lint errors in the console.

`npm test` - This command runs the test suite. See the section about [running tests](https://facebook.github.io/create-react-app/docs/running-tests) for more information.

`npm run build` - Builds the app for production to the `build` folder. This command prepares the code for deployment.

## Learn More

If this documentation wasn't clear enough, here is the [React documentation](https://reactjs.org/).
