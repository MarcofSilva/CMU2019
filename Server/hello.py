from flask import Flask,render_template,redirect,request, session
from flask_login import LoginManager, current_user, login_user, logout_user, login_required, UserMixin
from flask_sqlalchemy import SQLAlchemy
from flask import json
app = Flask(__name__)

login_manager = LoginManager()
login_manager.init_app(app)
login_manager.login_view = 'login' # the login view of your application
app.config['SECRET_KEY'] = "lkkajdghdadkglajkgah" # a secret key for your app TODO
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///../dbdir/test.db'
db = SQLAlchemy(app)

@app.route("/")
def home():
    print(current_user)
    if not current_user.is_anonymous:
        return "hey " + current_user.username + ":<a href='/protected'>Protected</a> <a href='/logout'>Logout</a>"
    else:
        return "home: <a href='/register'>Register</a><a href='/login'>Login</a> <a href='/protected'>Protected</a>"

@app.route('/protected')
@login_required
def protected():
    return "you have access to this! <a href='/'>Home</a>"

@app.route('/login', methods = ['POST', 'GET'])
def login():
    username = request.json.get('username', None)
    print(username)
    password = request.json.get('password', None)

    if username and password:
        if validateArgs(username,password):
            user = User.query.filter_by(username=username).first()
            login_user(user, remember=True)
            next = request.args.get('next')
            print("buu")
            if current_user.is_authenticated:
                print("baa")
                return current_user.username + " you are logged in"
    return "nope" #TODO

@app.route('/logout', methods=['GET'])
@login_required
def logout():
    logout_user()
    return "you are logged out"

@app.route('/register', methods=['POST'])
def register():
    username = request.json.get('username', None)
    print(username)
    password = request.json.get('password', None)
    #if 'username' in args and 'password' in args:
    users = User.query.filter_by(username=username) #TODO
    print(users.count())
    if users.count() != 0:
        return "Username taken"
    else:
        u = User(username=username, password=password)
        db.session.add(u)
        db.session.commit()
        return "Success!"



#This callback reloads the user from the ID stored in the session
@login_manager.user_loader
def get_user(user_id):
    return User.query.get(user_id)

class User(UserMixin, db.Model):
  id = db.Column(db.Integer, primary_key=True)
  username = db.Column(db.String(80), unique=True, nullable=False)
  password = db.Column(db.String(50), unique=False, nullable = False)

  def __repr__(self):
    return '<User %r %r>' % (self.id, self.username)

def validateArgs(username, password):
    if User.query.filter_by(username=username).count() == 0:
        return False
    user = User.query.filter_by(username=username).first()
    if user.password != password:
        return False
    return True
