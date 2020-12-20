import simple_grid
from q_learning_skeleton import *
import gym
import numpy as np
import time


def action_to_string(action_index):
    s ="{}".format(["Left","Down","Right","Up"][action_index])
    return s


def getShortestPath(begin, end, Q_table, env):
    """begin : is the starting state of our agent, in our case 0 row as it is the S in map.
        end : is the ending state (goal) of our agent, in our case row 47 as it is the G in map with reward +10.
        Q_table : is the "optimal" Q table we have calculated after training with episodes."""
    state = env.reset()
    path = [state]
    optimal_actions = []
    while state != end:
        next_action = np.argmax(Q_table[state, :])
        optimal_actions.append(next_action)
        new_state, reward, done, info = env.step(next_action)
        path.append(new_state)
        state = new_state
    return path, optimal_actions


def act_loop(env, agent, num_episodes):
    for episode in range(num_episodes):
        EPSILON = MIN_EXPLORATION_RATE + \
        (MAX_EXPLORATION_RATE - MIN_EXPLORATION_RATE) * np.exp(-EXPLORATON_DECAY_RATE*episode)
        print(EPSILON)
        #if EPSILON <= 0.2: time.sleep(3)
        state = env.reset()
        print('---episode %d---' % episode)
        renderit = False
        if episode % 10 == 0:
            renderit = True

        for t in range(MAX_EPISODE_LENGTH):
            if renderit:
                env.render()
            printing=False
            if t % 500 == 499:
                printing = True
    
            if printing:
                print('---stage %d---' % t)
                agent.report()
                print("state:", state)

            # pick a random action in case we need for 1-epsion case.
            random_action = env.action_space.sample()

            action = agent.select_action(state, random_action)
            
            new_state, reward, done, info = env.step(action)
        
            if printing:
                print("act:", action)
                print("reward=%s" % reward)

            agent.process_experience(state, action, new_state, reward, done)
            state = new_state
            if done:
                print("Episode finished after {} timesteps".format(t+1))
                env.render()
                agent.report()
                break

        #print(ql.q_table)
        #time.sleep(3)
    env.close()


if __name__ == "__main__":
    #env = simple_grid.DrunkenWalkEnv(map_name="walkInThePark")
    env = simple_grid.DrunkenWalkEnv(map_name="theAlley")
    num_a = env.action_space.n

    if (type(env.observation_space)  == gym.spaces.discrete.Discrete):
        num_o = env.observation_space.n
    else:
        raise("Qtable only works for discrete observations")

    discount = DEFAULT_DISCOUNT
    ql = QLearner(num_o, num_a, env.nrow, env.ncol, discount) #<- QTable
    act_loop(env, ql, NUM_EPISODES)
    # display the array without scientific notation.
    np.set_printoptions(suppress=True)

    print(ql.q_table)
    #Shortest_Path, optimal_actions = getShortestPath(0, 47, ql.q_table, env)
    
    # print(Shortest_Path)
    # optimal_path_actios = []
    # for step in optimal_actions:
    #     optimal_path_actios.append((action_to_string(step)))
    
    # print(optimal_path_actios)

