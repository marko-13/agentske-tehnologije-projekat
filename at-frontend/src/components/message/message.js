import React from 'react';
import axios from '../../axios';

class Message extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            msgContent: '',
            msgCountry: 0,
            msgRND: 0,
            msgAdministration: 0,
            msgMarketing: 0,
        }
        this.msgAgentSender = React.createRef();
        this.msgPerformative = React.createRef();
        this.msgAgentReciever = React.createRef();
    }

    onSubmit = e => {
        e.preventDefault();

        const senderString = this.msgAgentSender.current.value.split("!");
        const senderName = senderString[0];
        const senderType = senderString[1];

        const recieverString = this.msgAgentReciever.current.value.split("!");
        const recieverName = recieverString[0];
        const recieverType = recieverString[1];

        alert(senderName);
        alert(recieverName);
        alert(this.msgPerformative.current.value);

       const sendData = {
            performative: this.msgPerformative.current.value,
            //content: this.state.msgContent,
            //sender : senderName + "$192.168.1.9:8080$master$" + senderType + "$agents",
            //receivers: [recieverName + "$localhost:8080$master$" + recieverType + "$agents"]
            sender: senderName,
            receivers: [recieverName],
            params: this.state.msgCountry + '!' + this.state.msgRND + '!' + this.state.msgMarketing + '!' + this.state.msgAdministration
        }

        alert(this.state.msgCountry + '!' + this.state.msgRND + '!' + this.state.msgMarketing + '!' + this.state.msgAdministration);
        axios.post("rest/messages/", sendData)
            .then(res => alert(res.data))
            .catch(err => console.log(err.response));
    }

    render() {
        let form;

        form = (
            <form onSubmit={this.onSubmit}>
                <div className="form-group">
                    <label htmlFor="msgAgentSender">Sender:</label>
                    <select className="form-control mb-2" id="msgAgentSender" ref={this.msgAgentSender}>
                        {this.props.runningAgents.map(agent => {
                            return <option key={agent.name} value={`${agent.name}!${agent.type.name}`}>{agent.name} ({agent.type.name})</option>
                        })}
                    </select>
                </div>
                <div className="form-group">
                    <label htmlFor="msgPerformative">Performative:</label>
                    <select className="form-control mb-2" id="msgPerformative" ref={this.msgPerformative}>
                        {this.props.performatives.map(performative => {
                            return <option key={performative} value={performative}>{performative}</option>
                        })}
                        <option key="INFORM" value="INFORM">INFORM</option>
                        <option key="RERQUEST" value="REQUEST">REQUEST</option>
                    </select>
                </div>
                <div className="form-group">
                    <label htmlFor="msgContent">Content:</label>
                    <input
                        type="text"
                        className="form-control"
                        id="msgContent"
                        placeholder="Message content"
                        value={this.state.msgContent}
                        onChange={e => this.setState({ msgContent: e.target.value })} />
                </div>
                <div className="form-group">
                    <label htmlFor="msgAgentReciever">Reciever:</label>
                    <select className="form-control mb-2" id="msgAgentReciever" ref={this.msgAgentReciever}>
                        {this.props.runningAgents.map(agent => {
                            return <option key={agent.name} value={`${agent.name}!${agent.type.name}`}>{agent.name} ({agent.type.name})</option>
                        })}
                    </select>
                </div>

                {/* ADDED FIELDS */}
                <div className="form-group">
                    <label htmlFor="msgCountry">Country:</label>
                    <input
                        type="number"
                        className="form-control"
                        id="msgCountry"
                        placeholder="Country"
                        value={this.state.msgCountry}
                        onChange={e => this.setState({ msgCountry: e.target.value })} />
                </div>
                <div className="form-group">
                    <label htmlFor="msgRND">RnD:</label>
                    <input
                        type="number"
                        className="form-control"
                        id="msgRND"
                        placeholder="RnD"
                        value={this.state.msgRND}
                        onChange={e => this.setState({ msgRND: e.target.value })} />
                </div>
                <div className="form-group">
                    <label htmlFor="msgMarketing">Marketing:</label>
                    <input
                        type="number"
                        className="form-control"
                        id="msgMarketing"
                        placeholder="Marketing"
                        value={this.state.msgMarketing}
                        onChange={e => this.setState({ msgMarketing: e.target.value })} />
                </div>
                <div className="form-group">
                    <label htmlFor="msgAdministration">Administration:</label>
                    <input
                        type="number"
                        className="form-control"
                        id="msgAdministration"
                        placeholder="Administration"
                        value={this.state.msgAdministration}
                        onChange={e => this.setState({ msgAdministration: e.target.value })} />
                </div>

                <button type="submit" className="btn btn-dark float-right">Submit</button>
            </form>
        )

        if (this.props.runningAgents.length < 1) {
            alert("At least one agent must be started in order to send ACL message");
            form = <p>At least one agent must be started in order to send ACL message.</p>
        }

        return (
            <div className="container">
                {form}
            </div>
        )
    }
}

export default Message;