import React, { useState } from 'react';
import { Button } from 'react-bootstrap';

import cat from './cat.jpg';

function App() {
  const [showImage, setShowImage] = useState<boolean>(false);

  const handleButtonClick = () => {
    setShowImage(true);
  };

  return (
      <div>
        <Button variant="primary" onClick={handleButtonClick}>Do not press this button</Button>
        {showImage && <img src={cat} alt="" />}
          {/*<div>Hi</div>*/}
      </div>
  );
}

export default App;
