import React from 'react';

import '../../styles/common.css';
import '../../styles/objectDetailsEntry.css';

function ObjectDetailsEntry(props : {type: string, value: string}) {
    return (
        <div className="object-details-entry">
            <span className="object-details-entry-type">{props.type}</span>: <span className="object-details-entry-value">{props.value}</span>
        </div>
    )
}

export default ObjectDetailsEntry;