import React from 'react';
import './App.css';

import {
  BrowserRouter as Router,
  Switch,
  Route,
  Link
} from "react-router-dom";

import axios from './axios';
import Message from './components/message/message';
import Particles from 'react-particles-js';


const particleOptions = {
  particles: {
    number: {
      value: 90,
      density: {
        enable: true,
        value_area: 700
      }
    }
  },
  interactivity: {
    events: {
        onhover: {
            enable: true,
            mode: "repulse"
        }
    }
  }
}


class App extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      agentsTypes: [],
      agentsRunning: [],
      performatives: ['INFORM', 'REQUEST'],
      startAgentName: '',
      logger: []

    }
    this.startAgentType = React.createRef();
    this.stopAgentType = React.createRef();
  }

  socket = null;
  //host = "ws://localhost:8080/WAR2020/ws/";
  host = "ws://" + "192.168.1.9:8080" + "/2020WAR/ws/";
  //host = "ws://" + window.location.host + "/2020WAR/ws/";
  sessionId = '';


  componentDidMount() {

    axios.get('rest/agents/classes')
      .then(res => {
        console.log(res.data);
        this.setState({ agentsTypes: res.data });
      })
      .catch(err => console.log('Error occured while getting running agents'));

    this.fetchRunningAgents();
    this.fetchMessages();

    try {
      this.socket = new WebSocket(this.host + this.makeid(5));
      console.log('connect: Socket Status: ' + this.socket.readyState);
      console.log('Session opened on location: ' + window.location.hostname);

      this.socket.onopen = (evt) => {
        console.log('onopen: Socket Status: ' + this.socket.readyState + ' (open)');
      }

      this.socket.onmessage = msg => {
        const newLogger = [...this.state.logger];
        newLogger.push(msg.data);
        this.setState({logger: newLogger});

        const msgObject = JSON.parse(msg.data);
        alert(msgObject);
        // received public message
        if(msgObject.category === 0) {
          // $('#divMsg').append('<div class="container"><h4>From: '+msgObject.sender+'</h4><h5>Subject: '+msgObject.subject+'</h5><p>'+msgObject.content+'</p><hr></div>');
        }
        // received private message
        else if(msgObject.category === 1){
          // $('#divMsg').append('<div class="container"><h4>From: '+msgObject.sender+'</h4><h5>Subject: '+msgObject.subject+'</h5><p>'+msgObject.content+'</p><hr></div>');
        }
        // add user to active users list
        else if(msgObject.category === 2){
          // console.log('add user: ' + msgObject.content + ' to active users list');
          // $('#selectUser').append(new Option(msgObject.content, msgObject.content));
        }
        // remove user from active users list
        else if(msgObject.category === 3){
          // console.log('delete user: ' + msgObject.content + ' from active users list');
          // $('#selectUser option[value=' + msgObject.content + ']').remove();
        }
        else if (msgObject.category === 4){
          alert("STIGLA PORUKA KATEGORIJA 4 NOVI TIP AGENTA");
          alert(msgObject.content);
          axios.get('rest/agents/classes')
          .then(res => {
            console.log(res.data);
            this.setState({ agentsTypes: res.data });
          })
          .catch(err => console.log('Error occured while getting running agents'));
        }
        else if (msgObject.category === 5){
          alert("STIGLA PORUKA KATEGORIJA 5 BRISE TIP AGENTA");
          alert(msgObject.content);
          axios.get('rest/agents/classes')
          .then(res => {
            console.log(res.data);
            this.setState({ agentsTypes: res.data });
          })
          .catch(err => console.log('Error occured while getting running agents'));
        }
        else if (msgObject.category === 6){
          alert("STIGLA PORUKA KATEGORIJA 6 NOVI AGENT POKRENUT");
          alert(msgObject.content);
          this.fetchRunningAgents();
        }
        else if (msgObject.category === 7){
          alert("STIGLA PORUKA KATEGORIJA 7 AGENT ZAUSTAVLJEN");
          alert(msgObject.content);
          this.fetchRunningAgents();
        }
        else if (msgObject.category === 8){
          alert("STIGLA PORUKA KATEGORIJA 8 SALJI REZULTAT NA FRONT");
          alert(msgObject.content);
        }
      }

      this.socket.onclose = function () {
        console.log("WS closed")
        sessionStorage.setItem('username', '');
        this.socket = null;
      }

    } catch (e) {
      console.log('Socket exception: ' + e);
    }

  }

  makeid = (length) => {
    var result           = '';
    var characters       = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var charactersLength = characters.length;
    for ( var i = 0; i < length; i++ ) {
       result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
 }

  fetchRunningAgents = () => {
    axios.get('rest/agents/running')
      .then(res => this.setState({ agentsRunning: res.data }))
      .catch(err => console.log(err));
  }

  fetchMessages = () => {
    axios.get('rest/messages')
      .then(res => this.setState({ performatives: res.data }))
      .catch(err => console.log(err));
  }

  startAgentHandler = () => {
    axios.put(`rest/agents/running/${this.startAgentType.current.value}/${this.state.startAgentName}`)
      .then(res => {
        this.fetchRunningAgents();
        alert("Agent " + res.data.name + " started.");
        this.setState({ startAgentName: '' });
      })
      .catch(err => alert(err.response.data));
  }

  stopAgentHandler = () => {
    axios.delete(`rest/agents/running/${this.stopAgentType.current.value}`)
      .then(res => {
        this.fetchRunningAgents();
        alert("Agent " + res.data.name + " stopped.");
      })
      .catch(err => alert(err.response.data));
  }

  render() {
    return (
      <Router>
        <Particles className='particles' params={particleOptions}/>

        <div className="container">

          <h1 style={{ textAlign: 'center', marginBottom: '20px' }}></h1>
          <hr />

          {/* <div className="row"> */}
            <div className="col-2 p-3" >
              <div className="row">
                <button className="btn btn-dark my-button"><Link to="/agent-types">Agent Types</Link></button>
                <button className="btn btn-dark my-button"><Link to="/agents-running">Running Agents</Link></button>
                <button className="btn btn-dark my-button"><Link to="/start-agent">Start Agent</Link></button>
                <button className="btn btn-dark my-button"><Link to="/stop-agent">Stop Agent</Link></button>
                <button className="btn btn-dark my-button"><Link to="/send-message">Send Message</Link></button>
                <button className="btn btn-dark my-button"><Link to="/get-messages">Get Messages</Link></button>
              </div>
            </div>
            <div className="col-12">
              <Switch>
                <Route path="/send-message">
                  <Message
                    runningAgents={this.state.agentsRunning}
                    performatives={this.state.performatives} />
                </Route>
                <Route path="/start-agent">
                  <div className="container">
                    <form>
                      <div className="form-group">
                        <label htmlFor="startAgentSelect">Agent type:</label>
                        <select className="form-control mb-2" id="startAgentSelect" ref={this.startAgentType}>
                          {this.state.agentsTypes.map(agent => {
                            return <option key={agent.name} value={agent.name}>{agent.name}</option>
                          })}
                        </select>
                        <div className="form-group">
                          <label htmlFor="agentName">Agent name:</label>
                          <input
                            type="text"
                            className="form-control"
                            id="agentName"
                            placeholder="Enter agent name"
                            value={this.state.startAgentName}
                            onChange={e => this.setState({ startAgentName: e.target.value })} />
                        </div>
                        <button
                          type="button"
                          onClick={() => this.startAgentHandler()}
                          className="btn btn-dark m-2 float-right">Start</button>
                      </div>
                    </form>
                  </div>
                </Route>
                <Route path="/stop-agent">
                  <div className="container">
                    {this.state.agentsRunning.length === 0
                      ? <p>No agents running.</p>
                      : <form>
                        <div className="form-group">
                          <label htmlFor="stopAgentSelect">Stop agent:</label>
                          <select className="form-control mb-2" id="stopAgentSelect" ref={this.stopAgentType}>
                            {this.state.agentsRunning.map(agent => {
                              return <option key={agent.name} value={agent.name}>{agent.name} ({agent.type.name})</option>
                            })}
                          </select>
                          <button
                            type="button"
                            onClick={() => this.stopAgentHandler()}
                            className="btn btn-dark m-2 float-right">Stop</button>
                        </div>
                      </form>
                    }

                  </div>
                </Route>
                <Route path="/agent-types">
                  <div className="container">
                    <h5>Agent types</h5>
                    <ul>
                      {this.state.agentsTypes.map(agent => {
                        return <li key={agent.name}>{agent.name}</li>
                      })}
                    </ul>
                  </div>
                </Route>
                <Route path="/agents-running">
                  <div className="container">
                    {this.state.agentsRunning.length === 0
                      ? <p>No agents running.</p>
                      : <div>
                        <h5>Running agents</h5>

                        <ul>
                          {this.state.agentsRunning.map(agent => {
                            return <li key={agent.name}>{agent.name}</li>
                          })}
                        </ul>
                      </div>
                    }
                  </div>
                </Route>
                <Route path="/get-messages">
                  <div className="container">
                    <h5>Performatives</h5>
                    <ul>
                      {this.state.performatives.map(performative => <li key={performative}>{performative}</li>)}
                    </ul>
                  </div>
                </Route>
              </Switch>
            </div>
          {/* </div> */}
          <hr />
          <div className="logger">
            {this.state.logger.map(log => {
              return <p key={log}>{log}</p>
            })}
          </div>
        </div >
      </Router>
    )
  }

}

export default App;