import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';

import Map from './components/Map'

const root = ReactDOM.createRoot(
    document.getElementById('root') as HTMLElement
);
root.render(
    // Here is a bug that I had.
    // Apparently during strict mode development,
    // it's likely that it will to an additional render of the components to detect any side effects
    // this is weird, why would you do an additional render of the same thing ? :/
    // some people on internet say that it's okay to disable it, especially in production
    // not sure why it is enabled in the first place

    // <React.StrictMode>
        <Map />
    // </React.StrictMode>
);
