from flask import Flask,render_template,redirect,request
from flask_login import LoginManager, current_user, login_user, logout_user, login_required, UserMixin
from flask_sqlalchemy import SQLAlchemy
app = Flask(__name__)

login_manager = LoginManager()
login_manager.init_app(app)
login_manager.login_view = 'login' # the login view of your application
app.config['SECRET_KEY'] = "lkkajdghdadkglajkgah" # a secret key for your app TODO
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:////tmp/test.db'
db = SQLAlchemy(app)

@app.route("/")
def home():
    if not current_user.is_anonymous:
        return "hey " + current_user.username + ":<a href='/protected/'>Protected</a> <a href='/logout/'>Logout</a>"
    else:
        return "home: <a href='/register/'>Register</a><a href='/login/'>Login</a> <a href='/protected/'>Protected</a>"

@app.route('/protected/')
@login_required
def protected():
    return "protected"

@app.route('/login/')
def login():
    if (current_user.is_authenticated):
        return "current user: " + current_user.username + "<a href='/loginclick/'>Login</a>"
    else:
        return "Please login. <a href='/loginclick/'>Login</a>"

@app.route('/loginclick/')
def loginclick():
    #Where does the information come from? TODO
    user = User.query.get(1)
    db.session.add(user)
    db.session.commit()
    login_user(user)
    if current_user.is_authenticated:
        print(current_user.username)
        return current_user.username + " you are logged in <a href='/'>Home</a>"
    else:
        return "nope"

@app.route('/logout/')
@login_required
def logout():
    user = current_user
    db.session.add(user)
    db.session.commit()
    logout_user()
    return "you are logged out"

@app.route('/register/')
def register():
    users = User.query.filter_by(username='bob').first() #TODO
    print(users)
    print("---")
    print(type(users))
    if users != None:
        return "Username taken"
    else:
        u = User(username='bob', password='bob')
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