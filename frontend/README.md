# Frontend Documentation

## Two different modes of map rendering

In the folder `src/configs` there are some configuration files with values to change if wanted. Changing
them also helps to set the way the map renders ship markers. 
There are two different ways, described below, to do that. Of course, any other values
are also possible. Everything depends on the computer the application is run on.

1. **Beautiful clustering mode**. Map clusters the ships if there are two many on the screen. However, if used without filtering (described in the 2nd mode below), this may impact performance when there are many ships. To use this mode, set the following values in the config files:
```
// in websocketConfig.js:
    "websocketBufferRefreshMs": 5000 

// in mapConfig.js:
    "doFilteringBeforeDisplaying": false,
    "maxShipsOnScreen": -1,

    "clusterChunkedLoading": true,
    "clusterChunkInterval": 200,
    "clusterChunkDelay": 100,
    "clusterMaxRadius": 80
```

2. **Efficient mode**. To render the map faster, filtering should be turned on. Filtering means that only the ships that are seen on the current map view are rendered, and only up to some set limit (for example, only the 50 most anomalous ships). Since in this way not all ships are rendered, it is recommended to turn off the clustering. This is done by setting the clustering radius to 0. All constants that should be written to config files are as below:
```
// in websocketConfig.js:
    "websocketBufferRefreshMs": 1000 

// in mapConfig.js:
    "doFilteringBeforeDisplaying": true,
    "maxShipsOnScreen": 100,

    "clusterChunkedLoading": true,
    "clusterChunkInterval": 200,
    "clusterChunkDelay": 100,
    "clusterMaxRadius": 0
```


## Building the project

In order to work with the frontend, one has to install the NodeJS programming environment. This can be done by running:
```
sudo apt update
sudo apt install nodejs
node -v
```

Afterwards, locate to the frontend directory and run the following command to install the npm dependencies:

```
npm install
```

## Running the project

In order to run the project, one can use the following commands:

`npm start` - runs the app in the development mode. Open [http://localhost:3000](http://localhost:3000) to view it in the browser. The page will reload if you make edits. You will also see any lint errors in the console.

`npm test` - this command runs the test suite. See the section about [running tests](https://facebook.github.io/create-react-app/docs/running-tests) for more information.

`npm run build` - builds the app for production to the `build` folder. This command prepares the code for deployment.

## Additional information

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

## Learn More

If this documentation wasn't clear enough, here is the [React documentation](https://reactjs.org/).

## Icons

- bell-notifications.svg - taken from https://iconoir.com/
- settings.svg - taken from https://iconoir.com/
- ship.png - taken from https://www.freepik.com
- back.svg - taken from https://flowbite.com/icons/
- close.png - taken from https://flowbite.com/icons/
